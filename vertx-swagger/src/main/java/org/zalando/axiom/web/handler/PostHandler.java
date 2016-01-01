package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Operation;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;

public class PostHandler<T, R> implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostHandler.class);

    private final Operation operation;

    private final ObjectMapper mapper;

    private final Function<T, R> function;

    private final Class<T> paramType;

    public PostHandler(Operation operation, ObjectMapper mapper, Function<T, R> function, Class<T> paramType) {
        this.operation = operation;
        this.mapper = mapper;
        this.function = function;
        this.paramType = paramType;
    }

    public void handle(RoutingContext routingContext) {
        String bodyAsJson = routingContext.getBodyAsString("UTF-8");

        checkNotNull(bodyAsJson, "Body must not be null!");
        Object id;
        try {
            id = function.apply(mapper.readValue(bodyAsJson, paramType));
        } catch (Exception e) {
            LOGGER.error("Unexpected exception occurred!", e);
            routingContext.fail(500);
            return;
        }
        if (id == null) {
            handleNoResult(routingContext);
        } else {
            handleResult(routingContext, id);
        }
    }

    private void handleNoResult(RoutingContext routingContext) {
        LOGGER.error("No result returned.");
        routingContext.fail(500);
    }

    private void handleResult(RoutingContext routingContext, Object result) {
        // if method returns string it is taken as the id
        // otherwise the result object has to have a getId method, which returns string
        String id;
        if (result instanceof String) {
            id = result.toString();
        } else {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
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
        }

        HttpServerResponse response = routingContext.response();
        String path = routingContext.request().path();
        if (doSetLocation()) {
            response.headers().set(HttpHeaders.LOCATION, path + "/" + id);
        }
        response.setStatusCode(201);
        response.end();
    }

    private boolean doSetLocation() {
        return operation.getResponses().get("201").getHeaders().containsKey("Location");
    }
}
