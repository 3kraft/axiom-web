package org.zalando.axiom.web.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;

import static org.zalando.axiom.web.util.HandlerUtils.getOnlyValue;

public class DeleteHandler implements Handler<RoutingContext> {

    private final Consumer<String> function;

    public DeleteHandler(Consumer<String> function) {
        this.function = function;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String id = getOnlyValue(routingContext);
        try {
            function.accept(id);
            routingContext.response().setStatusCode(204).end();
        } catch (Exception e) {
            routingContext.fail(500);
        }
    }
}
