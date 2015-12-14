package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.binding.functions.StringFunction;

import java.util.function.IntFunction;
import java.util.function.Supplier;

import static org.zalando.axiom.web.util.HandlerUtils.getOnlyValue;

public final class GetWithZeroOrOneParameterHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetWithZeroOrOneParameterHandler.class);

    private final ObjectMapper mapper;

    private final Object function;

    public GetWithZeroOrOneParameterHandler(ObjectMapper mapper, Object function) {
        this.mapper = mapper;
        this.function = function;
    }

    public void handle(RoutingContext routingContext) {
        Object value;
        if (function instanceof StringFunction) {
            value = ((StringFunction) function).apply(getOnlyValue(routingContext));
        } else if (function instanceof Supplier) {
            value = ((Supplier) function).get();
        } else if (function instanceof IntFunction) {
            value = ((IntFunction) function).apply(Integer.parseInt(getOnlyValue(routingContext)));
        } else {
            throw new UnsupportedOperationException("Controller with this arity is not yet implemented!");
        }
        try {
            if (value == null) {
                routingContext.response().setStatusCode(404).end();
            } else {
                routingContext.response().setStatusCode(200).end(mapper.writeValueAsString(value));
            }
        } catch (Exception throwable) {
            LOGGER.error("Invoking controller method failed!", throwable);
            routingContext.fail(500);
        }
    }
}
