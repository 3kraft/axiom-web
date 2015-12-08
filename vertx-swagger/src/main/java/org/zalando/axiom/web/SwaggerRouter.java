package org.zalando.axiom.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.Property;
import io.swagger.parser.Swagger20Parser;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.domain.OperationTarget;
import org.zalando.axiom.web.exceptions.LoadException;
import org.zalando.axiom.web.handler.GetHandler;
import org.zalando.axiom.web.handler.PostHandler;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.zalando.axiom.web.util.Strings.camelToSnailCase;
import static org.zalando.axiom.web.util.Strings.toVertxPathParams;
import static org.zalando.axiom.web.util.Types.getParameterType;

public final class SwaggerRouter implements Router {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerRouter.class);

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
        router.route().handler(BodyHandler.create());

        checkPreconditions();
        String basePath = swagger.getBasePath();
        for (Map.Entry<String, Path> pathEntry : swagger.getPaths().entrySet()) {
            final String fullPath = basePath + pathEntry.getKey();

            Path path = pathEntry.getValue();
            Map<String, OperationTarget> operationTargets;
            try {
                operationTargets = getOperationTargets(path, swagger.getDefinitions());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                LOGGER.error("Could not get operation target method!", e);
                throw new IllegalStateException(e);
            }
            bindRoutes(fullPath, operationTargets);
        }
        return this;
    }

    private void bindRoutes(final String fullPath, final Map<String, OperationTarget> operationTargets) {
        LOGGER.debug("Binding route to path [{}].", fullPath);

        for (OperationTarget operationTarget : operationTargets.values()) {
            Handler<RoutingContext> handler;
            switch (operationTarget.getVertxHttpMethod()) {
                case GET:
                    handler = new GetHandler(operationTarget);
                    break;
                case POST:
                    handler = new PostHandler(operationTarget);
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Handler for http method [%s] not implemented!", operationTarget.getVertxHttpMethod()));

            }
            router.route(operationTarget.getVertxHttpMethod(), toVertxPathParams(fullPath)).handler(handler);
        }
    }


    private Map<String, OperationTarget> getOperationTargets(final Path path, Map<String, Model> definitions) throws NoSuchMethodException, IllegalAccessException {
        final Map<String, OperationTarget> results = new HashMap<>();

        for (Map.Entry<io.swagger.models.HttpMethod, Operation> operationMap : path.getOperationMap().entrySet()) {
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
            checkMethodParameters(targetMethod, operation, definitions);

            MethodHandle methodHandle = getMethodHandle(className, methodName, targetMethod, operation);

            results.put(operationId, new OperationTarget(httpMethod, operation, target, targetMethod, methodHandle));
        }
        return results;
    }

    private void checkMethodParameters(Method targetMethod, Operation operation, Map<String, Model> definitions) {
        java.lang.reflect.Parameter[] parameters = targetMethod.getParameters();
        List<Parameter> operationParameters = operation.getParameters();

        if (parameters.length != operationParameters.size()) {
            throw new IllegalStateException(String.format("Parameter count of method [%s] does not match to operation parameter count [%s]!",
                    targetMethod.getName(), operation.getOperationId()));
        }

        for (int i = 0; i < parameters.length; ++i) {
            java.lang.reflect.Parameter parameter = parameters[i];
            Parameter operationParameter = operationParameters.get(i);

            if (operationParameter instanceof QueryParameter) {

                checkType(targetMethod.getName(), operation, parameter, (QueryParameter) operationParameter);

            } else if (operationParameter instanceof PathParameter) {

                checkType(targetMethod.getName(), operation, parameter, (PathParameter) operationParameter);

            } else if (operationParameter instanceof BodyParameter) {
                BodyParameter bodyParameter = (BodyParameter) operationParameter;
                String ref = bodyParameter.getSchema().getReference();
                Model model = getModel(ref, definitions);

                if (targetMethod.getParameters().length != 1) {
                    throw new IllegalStateException(String.format("Method [%s] must have exactly one parameter matching [%s]!", targetMethod.getName(), ref));
                }

                checkFields(parameter, ref, model);
                // no-op
            } else {
                throw new UnsupportedOperationException(String.format("Unhandled parameter type [%s].", operationParameter.getClass().getName()));
            }
        }
    }

    private void checkType(String targetMethodName, Operation operation, java.lang.reflect.Parameter parameter, AbstractSerializableParameter modelParameter) {
        if (parameter.getType() != getParameterType(modelParameter.getType(), modelParameter.getFormat())) {
            throw new IllegalStateException(String.format("Parameter types in method [%s] are not matching types for operation id [%s].",
                    targetMethodName, operation.getOperationId()));
        }
    }

    private void checkFields(java.lang.reflect.Parameter parameter, String ref, Model model) {
        for (Field field : parameter.getType().getDeclaredFields()) {
            String fieldName = field.getName();
            Map<String, Property> properties = model.getProperties();
            Property property = properties.get(convertFieldNameFromJava(fieldName));
            if (property == null) {
                throw new IllegalStateException(String.format("Field [%s] not found in model [%s]!", fieldName, ref));
            }
            if (field.getType() != getParameterType(property.getType(), property.getFormat())) {
                throw new IllegalStateException(String.format("Type of field [%s] does not match in domain object and model [%s]!", fieldName, ref));
            }
        }
    }

    private String convertFieldNameFromJava(String fieldName) {
        return camelToSnailCase(fieldName);
    }

    private Model getModel(String ref, Map<String, Model> definitions) {
        final String modelName = ref.substring(ref.lastIndexOf("/") + 1); // sample "#/definitions/Product"
        Model model = definitions.get(modelName);
        if (model == null) {
            throw new IllegalStateException(String.format("Model with name [%s] and ref [%s] not found in swagger definitions!", modelName, ref));
        }
        return model;
    }

    private MethodHandle getMethodHandle(String className, String methodName, Method targetMethod, Operation operation) throws NoSuchMethodException, IllegalAccessException {
        LOGGER.debug("Getting method handle for method [{}] in class [{}].", methodName, className);

        List<Class<?>> parameterTypes = new LinkedList<>();
        parameterTypes.add(Object.class); // add target type
        for (Parameter parameter : operation.getParameters()) {
            if (parameter instanceof QueryParameter) {
                QueryParameter queryParameter = (QueryParameter) parameter;
                parameterTypes.add(getParameterType(queryParameter.getType(), queryParameter.getFormat()));
            } else if (parameter instanceof PathParameter) {
                PathParameter pathParameter = (PathParameter) parameter;
                parameterTypes.add(getParameterType(pathParameter.getType(), pathParameter.getFormat()));
            } else if (parameter instanceof BodyParameter) {
                parameterTypes.add(targetMethod.getParameters()[0].getType());
            } else {
                throw new UnsupportedOperationException("other parameter types still to be implemented");
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

        return methodHandle.asType(methodType);
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

