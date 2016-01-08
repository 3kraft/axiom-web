package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Operation;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.binding.functions.AsyncConsumer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;

public class PostHandler<T> extends DefaultRouteHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostHandler.class);

    private final Operation operation;

    private final ObjectMapper mapper;

    private final Object function;

    private final Class<T> paramType;

    public PostHandler(Operation operation, ObjectMapper mapper, Object function, Class<T> paramType) {
        this.operation = operation;
        this.mapper = mapper;
        this.function = function;
        this.paramType = paramType;
    }

    public void handle(RoutingContext routingContext) {
        String bodyAsJson = routingContext.getBodyAsString("UTF-8");

        checkNotNull(bodyAsJson, "Body must not be null!");
        executeFunction(routingContext, bodyAsJson, id -> {
            if (id == null) {
                handleNoResult(routingContext);
            } else {
                handleResult(routingContext, id);
            }
        });

    }

    @SuppressWarnings("unchecked") // async functions with generic consumers cause not nice warnings
    private void executeFunction(RoutingContext routingContext, String bodyAsJson, Consumer<Object> callback) {
        try {
            if (function instanceof AsyncConsumer) {
                ((AsyncConsumer) function).accept(mapper.readValue(bodyAsJson, paramType), defaultAsyncResultHandler(routingContext, callback));
            }
            else if (function instanceof Function) {
                Object id = ((Function) function).apply(mapper.readValue(bodyAsJson, paramType));
                callback.accept(id);
            }
            else {
                throw new UnsupportedOperationException(String.format("Controller with this arity is not yet implemented: [%s]", function.getClass().getName()));
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected exception occurred!", e);
            routingContext.fail(500);
        }
    }

    private void handleNoResult(RoutingContext routingContext) {
        LOGGER.error("No result returned.");
        routingContext.fail(500);
    }

    private void handleResult(RoutingContext routingContext, Object result) {
        HttpServerResponse response = routingContext.response();
        if (doSetLocation()) {
            // If method returns String, then it is taken as the id,
            // otherwise the result object has to have a getId method, which returns string.
            final String id = result instanceof String ? result.toString() : getIdFromResult(result);

            final String path = routingContext.request().path();
            response.headers().set(HttpHeaders.LOCATION, path + "/" + id);
        }
        response.setStatusCode(201);
        response.end();
    }

    private String getIdFromResult(Object result) {
        String id;MethodHandles.Lookup lookup = MethodHandles.lookup();
        Object idObject = null;
        try {
            MethodHandle getIdMethodHandle = lookup.findVirtual(result.getClass(), "getId", MethodType.methodType(String.class));
            idObject = getIdMethodHandle.invoke(result);
        } catch (Throwable throwable) {
            LOGGER.error("Could not call method to retrieve id!", throwable);
        }
        if (idObject == null) {
            throw new IllegalStateException(String.format("Id method on [%s] returned null!", result.getClass()));
        }
        id = idObject.toString();
        return id;
    }

    private boolean doSetLocation() {
        return operation.getResponses().get("201").getHeaders().containsKey("Location");
    }
}
