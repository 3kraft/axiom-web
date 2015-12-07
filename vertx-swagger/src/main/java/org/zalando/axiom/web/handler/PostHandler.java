package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.domain.OperationTarget;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class PostHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostHandler.class);

    private final ObjectMapper mapper;

    private final OperationTarget operationTarget;

    public PostHandler(OperationTarget operationTarget) {
        this.operationTarget = operationTarget;
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String bodyAsJson = routingContext.getBodyAsString("UTF-8");

        Method targetMethod = operationTarget.getTargetMethod();
        Object target = operationTarget.getTarget();
        try {
            Object result = operationTarget.getTargetMethodHandle().invoke(target, mapper.readValue(bodyAsJson, targetMethod.getParameterTypes()[0]));

            // if method returns string it is taken as the id
            // otherwise the result object has to have a getId method, which returns string
            String id;
            if (result instanceof String) {
                id = result.toString();
            } else {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle getIdMethodHandle = lookup.findVirtual(result.getClass(), "getId", MethodType.methodType(String.class));
                Object idObject = getIdMethodHandle.invoke(result);
                if (idObject == null) {
                    throw new IllegalStateException(String.format("Id method on [%s] returned null!", result.getClass()));
                }
                id = idObject.toString();
            }

            HttpServerResponse response = routingContext.response();
            response.headers().set(HttpHeaders.LOCATION, routingContext.request().path() + "/" + id);
            response.setStatusCode(201);
            response.end();

        } catch (Throwable throwable) {
            LOGGER.error("Failed to call method [{}] on target [{}]", targetMethod.getName(), target, throwable);
            routingContext.fail(500);
        }

    }
}
