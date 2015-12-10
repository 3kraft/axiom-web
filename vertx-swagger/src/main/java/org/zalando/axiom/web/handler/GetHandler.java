package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.binding.functions.DoubleDoubleFunction;
import org.zalando.axiom.web.binding.functions.IntIntFunction;
import org.zalando.axiom.web.binding.functions.StringFunction;

import java.util.function.IntFunction;
import java.util.function.Supplier;

import static org.zalando.axiom.web.util.HandlerUtils.getOnlyValue;

public final class GetHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetHandler.class);

    private final ObjectMapper mapper;

    private final Object function;

    public GetHandler(ObjectMapper mapper, Object function) {
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
        } else if (function instanceof IntIntFunction) {
            value = ((IntIntFunction) function).apply(Integer.parseInt("-1"), Integer.parseInt("-1")); // FXIME match up parameters by name
        } else if (function instanceof DoubleDoubleFunction) {
            value = ((DoubleDoubleFunction) function).apply(Double.parseDouble("-1"), Double.parseDouble("-1")); // FXIME match up parameters by name
        } else {
            throw new UnsupportedOperationException("Controller with this arity is not yet implemented!");
        }
        try {
            routingContext.response().end(mapper.writeValueAsString(value));
        } catch (Exception throwable) {
            LOGGER.error("Invoking controller method failed!", throwable);
            routingContext.fail(500);
        }
    }
}
