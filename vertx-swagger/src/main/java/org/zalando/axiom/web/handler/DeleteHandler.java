package org.zalando.axiom.web.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.binding.functions.AsyncStringConsumer;

import static org.zalando.axiom.web.util.HandlerUtils.getOnlyValue;

public class DeleteHandler implements Handler<RoutingContext> {

    private final AsyncStringConsumer function;

    public DeleteHandler(AsyncStringConsumer function) {
        this.function = function;
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
