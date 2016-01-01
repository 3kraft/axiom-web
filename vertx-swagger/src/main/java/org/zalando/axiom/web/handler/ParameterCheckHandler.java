package org.zalando.axiom.web.handler;

import io.swagger.models.Operation;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

public class ParameterCheckHandler implements Handler<RoutingContext> {

    private final Operation operation;

    public ParameterCheckHandler(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        List<String> validationResult = operation.getParameters().stream().map(parameter -> {
            String name = parameter.getName();
            if (parameter.getRequired() && !request.params().contains(name)) {
                return String.format("Parameter [%s] is required but missing!", name);
            } else {
                return "";
            }
        }).filter(Strings::isNotBlank).collect(Collectors.toList());

        if (validationResult.isEmpty()) {
            routingContext.next();
        } else {
            routingContext.response().setStatusCode(400).end(String.join("\n", validationResult));
        }
    }

}
