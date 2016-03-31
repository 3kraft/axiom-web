package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResultHandler;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.binding.functions.AsyncSupplier;
import org.zalando.axiom.web.binding.functions.unary.AsyncBooleanFunction;
import org.zalando.axiom.web.binding.functions.unary.AsyncDateFunction;
import org.zalando.axiom.web.binding.functions.unary.AsyncDoubleFunction;
import org.zalando.axiom.web.binding.functions.unary.AsyncFloatFunction;
import org.zalando.axiom.web.binding.functions.unary.AsyncIntFunction;
import org.zalando.axiom.web.binding.functions.unary.AsyncLongFunction;
import org.zalando.axiom.web.binding.functions.unary.AsyncStringFunction;
import org.zalando.axiom.web.util.Types;

import java.util.Date;

import static org.zalando.axiom.web.util.HandlerUtils.getOnlyValue;

public final class GetWithZeroOrOneParameterHandler extends DefaultRouteHandler {

    private final ObjectMapper mapper;

    private final Object function;

    public GetWithZeroOrOneParameterHandler(ObjectMapper mapper, Object function) {
        this.mapper = mapper;
        this.function = function;
    }

    public void handle(RoutingContext routingContext) {
        executeAsyncFunction(routingContext, asyncResult -> {
            if (asyncResult == null) {
                routingContext.response().setStatusCode(404).end();
            } else if (asyncResult.failed()) {
                routingContext.fail(500);
            } else if (asyncResult.result() == null) {
                routingContext.response().setStatusCode(404).end();
            } else {
                try {
                    routingContext.response().setStatusCode(200).end(mapper.writeValueAsString(asyncResult.result()));
                } catch (JsonProcessingException e) {
                    handleError(String.format("Could not serialize result [%s]!", routingContext.currentRoute().getPath()), e, routingContext);
                }
            }
        });
    }

    @SuppressWarnings("unchecked") // async functions with generic consumers cause not nice warnings
    private <T> void executeAsyncFunction(RoutingContext routingContext, AsyncResultHandler<T> handler) {
        if (function instanceof AsyncSupplier) {
            ((AsyncSupplier) function).get(handler);
        } else {
            String value = getOnlyValue(routingContext);
            if (function instanceof AsyncStringFunction) {
                ((AsyncStringFunction) this.function).apply(value, handler);
            } else if (function instanceof AsyncIntFunction) {
                ((AsyncIntFunction) function).apply((Integer) Types.castValueToType(value, int.class), handler);
            } else if (function instanceof AsyncBooleanFunction) {
                ((AsyncBooleanFunction) function).apply((Boolean) Types.castValueToType(value, boolean.class), handler);
            } else if (function instanceof AsyncDoubleFunction) {
                ((AsyncDoubleFunction) function).apply((Double) Types.castValueToType(value, double.class), handler);
            } else if (function instanceof AsyncFloatFunction) {
                ((AsyncFloatFunction) function).apply((Float) Types.castValueToType(value, float.class), handler);
            } else if (function instanceof AsyncLongFunction) {
                ((AsyncLongFunction) function).apply((Long) Types.castValueToType(value, long.class), handler);
            } else if (function instanceof AsyncDateFunction) {
                ((AsyncDateFunction) function).apply((Date) Types.castValueToType(value, Date.class), handler);
            } else {
                throw new UnsupportedOperationException(String.format("Async controller with this arity is not yet implemented: [%s]", function.getClass().getName()));
            }
        }
    }

}
