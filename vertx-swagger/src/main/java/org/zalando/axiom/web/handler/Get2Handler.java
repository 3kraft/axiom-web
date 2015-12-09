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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zalando.axiom.web.util.Types.castValueToType;

public final class Get2Handler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Get2Handler.class);

    private final ObjectMapper mapper;

    private final Function function;

    public Get2Handler(ObjectMapper mapper, Function function) {
        this.mapper = mapper;
        this.function = function;
    }

    @SuppressWarnings("unchecked")
    public void handle(RoutingContext routingContext) {
        String value = routingContext.request().params().iterator().next().getValue();

        // FIXME cast value to function T
        try {
            routingContext.response().end(mapper.writeValueAsString(function.apply(value)));
        } catch (Throwable throwable) {
            LOGGER.error("Invoking controller method failed!", throwable);
            routingContext.fail(500);
        }
    }
}
