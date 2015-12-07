package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.domain.OperationTarget;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zalando.axiom.web.util.Types.castValueToType;

public final class GetHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetHandler.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final OperationTarget operationTarget;

    public GetHandler(OperationTarget operationTarget) {
        this.operationTarget = operationTarget;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(RoutingContext routingContext) {
        MultiMap params = routingContext.request().params();
        List parameters = new LinkedList();
        parameters.add(operationTarget.getTarget());
        parameters.addAll(params.names().stream().map(name -> castValueToType(params.get(name), operationTarget.getParameterType(name))).collect(Collectors.toList()));
        // TODO validate parameters according to swagger
        // TODO invoke exact for get by id
        try {
            Object o = operationTarget.getTargetMethodHandle().invokeWithArguments(parameters);
            routingContext.response().end(mapper.writeValueAsString(o));
        } catch (Throwable throwable) {
            LOGGER.error("Invoking controller method failed!", throwable);
            routingContext.fail(500);
        }
    }
}
