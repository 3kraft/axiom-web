package org.zalando.axiom.web.handler;

import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.binding.functions.AsyncStringConsumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.zalando.axiom.web.util.HandlerUtils.getOnlyValue;

public class DeleteHandler implements Handler<RoutingContext> {

    private final Operation operation;

    private final AsyncStringConsumer function;

    private final static HashSet<String> ACCEPTED_RESPONSE_CODES = new HashSet<>();

    static {
        ACCEPTED_RESPONSE_CODES.add("204");
        ACCEPTED_RESPONSE_CODES.add("500");
    }

    public DeleteHandler(Operation operation, AsyncStringConsumer function) {
        this.operation = operation;
        this.function = function;
        checkResponses(operation.getResponses());
    }

    private void checkResponses(Map<String, Response> responses) {
        if (! responses.keySet().containsAll(ACCEPTED_RESPONSE_CODES)) {
            throw new IllegalStateException("Swagger definition must contain status codes 204 and 500 in response definition!");
        }
    }

    @Override
    public void handle(RoutingContext routingContext) {

        String id = getOnlyValue(routingContext);
        try {
            function.accept(id, asyncResult -> {
                if (asyncResult.succeeded()) {
                    routingContext.response().setStatusCode(204).end();
                } else {
                    routingContext.fail(500);
                }
            });
        } catch (Exception e) {
            routingContext.fail(500);
        }
    }
}
