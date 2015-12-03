package org.zalando.axiom.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.parser.Swagger20Parser;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.domain.OperationTarget;
import org.zalando.axiom.web.exceptions.LoadException;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwaggerRouter implements Router {

    private final Router router;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, Object> controllers = new HashMap<>();

    private SwaggerRouter(Router router) {
        this.router = router;
    }

    public static SwaggerRouter router(Vertx vertx) {
        return new SwaggerRouter(Router.router(vertx));
    }

    public Router setupRoutes(InputStream jsonStream) {
        return setupRoutes(load(jsonStream));
    }

    public Router setupRoutes(java.nio.file.Path jsonPath) {
        return setupRoutes(load(jsonPath));
    }

    public SwaggerRouter controller(Object controller) {
        if (controller == null) {
            throw new NullPointerException("A controller must not be null!");
        }
        controllers.put(controller.getClass().getName(), controller);
        return this;
    }

    private void checkPreconditions() {
        if (this.controllers.isEmpty()) {
            throw new IllegalStateException("Controllers must be set!");
        }
    }

    private Router setupRoutes(Swagger swagger) {
        checkPreconditions();
        String basePath = swagger.getBasePath();
        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            final String fullPath = basePath + pathEntry.getKey();

            Path path = pathEntry.getValue();
            Map<String, OperationTarget> operationTargets = null;
            try {
                operationTargets = getOperationTargets(path);
            } catch (NoSuchMethodException | IllegalAccessException e){
                throw new IllegalStateException(e); // FIXME
            }
            bindRoutes(fullPath, operationTargets);
        }
        return this;
    }

    private void bindRoutes(final String fullPath, final Map<String, OperationTarget> operationTargets) {
        for (OperationTarget operationTarget : operationTargets.values()) {
            router.route(operationTarget.getVertxHttpMethod(), fullPath).handler(routingContext -> {
                MultiMap params = routingContext.request().params();
                List parameters = new LinkedList();
                parameters.add(operationTarget.getTarget());
                parameters.addAll(params.names().stream().map(name -> castValueToType(params.get(name), operationTarget.getParameterType(name))).collect(Collectors.toList()));
                // TODO validate parameters according to swagger
                // TODO invoke exact for get by id
                try {
                    Object o = operationTarget.getTargetMethodHandle().invokeWithArguments(parameters);
                    routingContext.response().end(mapper.writeValueAsString(o));
                } catch (Throwable throwable) {
                    // TODO log error
                    throwable.printStackTrace();
                    routingContext.fail(500);
                }
            });
        }
    }

    private Object castValueToType(String value, Class<?> parameterType) {
        if (parameterType == double.class) {
            return Double.parseDouble(value);
        } else {
            throw new UnsupportedOperationException(String.format("Unhandled type [%s].", parameterType.getName()));
        }
    }

    private Map<String, OperationTarget> getOperationTargets(final Path path) throws NoSuchMethodException, IllegalAccessException {
        final Map<String, OperationTarget> results = new HashMap<>();

        for (Map.Entry<io.swagger.models.HttpMethod, Operation> operationMap: path.getOperationMap().entrySet()) {
            final io.swagger.models.HttpMethod httpMethod = operationMap.getKey();
            final Operation operation = operationMap.getValue();

            final String operationId = operation.getOperationId();
            if (operationId == null) {
                throw new IllegalArgumentException(String.format("Operation id must be filled out! Format: [fully qualified class name + . + method name]. Operation summary [%s].", operation.getSummary()));
            }
            final int dotIndex = operationId.lastIndexOf('.');
            final String className = operationId.substring(0, dotIndex);
            final String methodName = operationId.substring(dotIndex + 1);

            final Object target = controllers.get(className);
            if (target == null) {
                throw new IllegalStateException(String.format("No controller of type [%s] registered for operation id [%s]", className, operationId));
            }

            Method targetMethod = null;
            for (Method method : target.getClass().getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    targetMethod = method;
                }
            }
            if (targetMethod == null) {
                throw new IllegalStateException(String.format("Method [%s] in controller [%s] not found", methodName, className));
            }

            MethodHandle methodHandle = getMethodHandle(className, methodName, targetMethod, operation);

            results.put(operationId, new OperationTarget(httpMethod, operation, target, targetMethod, methodHandle));
        }
        return results;
    }

    private MethodHandle getMethodHandle(String className, String methodName, Method targetMethod, Operation operation) throws NoSuchMethodException, IllegalAccessException {
        List<Class<?>> parameterTypes = new LinkedList<>();
        parameterTypes.add(Object.class); // add target type
        for (Parameter parameter: operation.getParameters()) {
            if (parameter instanceof QueryParameter) {
                QueryParameter queryParameter = (QueryParameter) parameter;
                parameterTypes.add(getParameterType(queryParameter));
            } else {
                throw new UnsupportedOperationException("other parameter types still to be implemented"); // FIXME
            }
        }

        MethodType methodType;
        if (targetMethod.getReturnType() == void.class) {
            methodType = MethodType.methodType(void.class, parameterTypes);
        } else {
            methodType = MethodType.methodType(Object.class, parameterTypes);
        }
        if (methodType == null) {
            throw new IllegalStateException(String.format("Could not determine method type for method [%s] and controller [%s]", methodName, className));
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle methodHandle = lookup.unreflect(targetMethod);
        MethodHandle adaptedMethodHandle = methodHandle.asType(methodType);

        return adaptedMethodHandle;
    }

    private Class<?> getParameterType(QueryParameter queryParameter) {
        switch (queryParameter.getType()) {
            case "number":
                switch (queryParameter.getFormat()) {
                    case "integer": return int.class;
                    case "long": return long.class;
                    case "float": return float.class;
                    case "double": return double.class;
                    default: return int.class;
                }
            case "integer":
                switch (queryParameter.getFormat()) {
                    case "integer": return int.class;
                    case "long": return long.class;
                    default: return int.class;
                }
            case "string": return String.class;
            case "boolean": return boolean.class;
            default: throw new UnsupportedOperationException(String.format("Type [%s] format [%s] not handled.", queryParameter.getType(), queryParameter.getFormat()));
        }
    }

    @Override
    public void accept(HttpServerRequest request) {
        router.accept(request);
    }

    @Override
    public Route route() {
        return router.route();
    }

    @Override
    public Route route(HttpMethod method, String path) {
        return router.route(method, path);
    }

    @Override
    public Route route(String path) {
        return router.route(path);
    }

    @Override
    public Route routeWithRegex(HttpMethod method, String regex) {
        return router.routeWithRegex(method, regex);
    }

    @Override
    public Route routeWithRegex(String regex) {
        return router.routeWithRegex(regex);
    }

    @Override
    public Route get() {
        return router.get();
    }

    @Override
    public Route get(String path) {
        return router.get(path);
    }

    @Override
    public Route getWithRegex(String regex) {
        return router.getWithRegex(regex);
    }

    @Override
    public Route head() {
        return router.head();
    }

    @Override
    public Route head(String path) {
        return router.head(path);
    }

    @Override
    public Route headWithRegex(String regex) {
        return router.headWithRegex(regex);
    }

    @Override
    public Route options() {
        return router.options();
    }

    @Override
    public Route options(String path) {
        return router.options(path);
    }

    @Override
    public Route optionsWithRegex(String regex) {
        return router.optionsWithRegex(regex);
    }

    @Override
    public Route put() {
        return router.put();
    }

    @Override
    public Route put(String path) {
        return router.put(path);
    }

    @Override
    public Route putWithRegex(String regex) {
        return router.putWithRegex(regex);
    }

    @Override
    public Route post() {
        return router.post();
    }

    @Override
    public Route post(String path) {
        return router.post(path);
    }

    @Override
    public Route postWithRegex(String regex) {
        return router.postWithRegex(regex);
    }

    @Override
    public Route delete() {
        return router.delete();
    }

    @Override
    public Route delete(String path) {
        return router.delete(path);
    }

    @Override
    public Route deleteWithRegex(String regex) {
        return router.deleteWithRegex(regex);
    }

    @Override
    public Route trace() {
        return router.trace();
    }

    @Override
    public Route trace(String path) {
        return router.trace(path);
    }

    @Override
    public Route traceWithRegex(String regex) {
        return router.traceWithRegex(regex);
    }

    @Override
    public Route connect() {
        return router.connect();
    }

    @Override
    public Route connect(String path) {
        return router.connect(path);
    }

    @Override
    public Route connectWithRegex(String regex) {
        return router.connectWithRegex(regex);
    }

    @Override
    public Route patch() {
        return router.patch();
    }

    @Override
    public Route patch(String path) {
        return router.patch(path);
    }

    @Override
    public Route patchWithRegex(String regex) {
        return router.patchWithRegex(regex);
    }

    @Override
    public List<Route> getRoutes() {
        return router.getRoutes();
    }

    @Override
    public Router clear() {
        return router.clear();
    }

    @Override
    public Router mountSubRouter(String mountPoint, Router subRouter) {
        return router.mountSubRouter(mountPoint, subRouter);
    }

    @Override
    public Router exceptionHandler(Handler<Throwable> exceptionHandler) {
        return router.exceptionHandler(exceptionHandler);
    }

    @Override
    public void handleContext(RoutingContext context) {
        router.handleContext(context);
    }

    @Override
    public void handleFailure(RoutingContext context) {
        router.handleFailure(context);
    }

    private Swagger load(InputStream jsonStream) {
        try (InputStreamReader reader = new InputStreamReader(jsonStream); Reader bufferedReader = new BufferedReader(reader)) {
            return new Swagger20Parser().read(mapper.readTree(bufferedReader));
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }

    private Swagger load(java.nio.file.Path jsonPath) {
        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            return new Swagger20Parser().read(mapper.readTree(reader));
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }
}

