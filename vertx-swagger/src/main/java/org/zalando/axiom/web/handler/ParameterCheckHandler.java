package org.zalando.axiom.web.handler;

import io.swagger.models.Operation;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class ParameterCheckHandler implements Handler<RoutingContext> {

    private final Operation operation;

    public ParameterCheckHandler(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        boolean validationFailed = operation.getParameters().stream().map(parameter -> {
            if (parameter.getRequired() && !request.params().contains(parameter.getName())) {
                routingContext.response().setStatusCode(400).end("foo");
                return true;
            } else {
                return false;
            }
        }).anyMatch(elem -> elem);

        if (!validationFailed) {
            routingContext.next();
        }
    }

}
