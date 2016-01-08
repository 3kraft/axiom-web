package org.zalando.axiom.web.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public abstract class DefaultRouteHandler implements Handler<RoutingContext>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRouteHandler.class);

    protected <T> AsyncResultHandler defaultAsyncResultHandler(RoutingContext routingContext, Consumer<T> callback) {
        return new AsyncResultHandler<T>() {
            @Override
            public void handle(AsyncResult<T> event) {
                if (event.succeeded()) {
                    callback.accept(event.result());
                }
                else {
                    handleControllerError(routingContext, event.cause());
                }
            }
        };
    }

    protected void handleControllerError(RoutingContext routingContext, Throwable cause) {
        String path = routingContext.currentRoute() != null ? routingContext.currentRoute().getPath() : null;
        handleError(String.format("Invoking controller method failed: [%s]", path), cause, routingContext);
    }

    protected void handleError(String message, Throwable cause, RoutingContext routingContext) {
        LOGGER.error(message, cause);
        routingContext.fail(500);
    }

}
