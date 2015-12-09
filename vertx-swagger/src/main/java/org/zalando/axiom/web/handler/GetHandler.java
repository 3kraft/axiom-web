package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.domain.OperationTarget;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.zalando.axiom.web.util.Types.castValueToType;

public final class GetHandler extends AbstractHttpMethodHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetHandler.class);

    public GetHandler(ObjectMapper mapper, OperationTarget operationTarget) {
        super(mapper, operationTarget);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(RoutingContext routingContext) {
        MultiMap params = routingContext.request().params();
        List parameters = new LinkedList();
        parameters.add(operationTarget.getTarget());
        if (params.size() == 1) {
            parameters.add(castValueToType(params.get(params.names().iterator().next()), operationTarget.getParameterTypeFromOnly()));
        } else {
            parameters.addAll(params.names().stream().map(name -> castValueToType(params.get(name), operationTarget.getParameterType(name))).collect(Collectors.toList()));
        }
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
