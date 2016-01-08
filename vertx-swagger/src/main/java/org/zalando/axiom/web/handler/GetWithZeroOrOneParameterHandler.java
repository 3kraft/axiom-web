package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.binding.functions.Async;
import org.zalando.axiom.web.binding.functions.AsyncIntFunction;
import org.zalando.axiom.web.binding.functions.AsyncStringFunction;
import org.zalando.axiom.web.binding.functions.AsyncSupplier;
import org.zalando.axiom.web.binding.functions.StringFunction;

import java.util.function.Consumer;
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
        executeFunction(routingContext, value -> {
                if (value == null) {
                    routingContext.response().setStatusCode(404).end();
                } else {
                    try {
                        routingContext.response().setStatusCode(200).end(mapper.writeValueAsString(value));
                    } catch (JsonProcessingException e) {
                        LOGGER.error(String.format("Could not serialize result [%s]!", routingContext.currentRoute().getPath()), e);
                        routingContext.fail(500);
                    }
                }
        });
    }

    private void executeFunction(RoutingContext routingContext, Consumer<Object> callback) {
            if (function instanceof Async) {
                executeAsyncFunction(routingContext, callback);
            }
            else {
                executeBlockingFunction(routingContext, callback);
            }
    }

    @SuppressWarnings("unchecked") // async functions with generic consumers cause not nice warnings
    private void executeAsyncFunction(RoutingContext routingContext, Consumer<Object> callback) {
        if (function instanceof AsyncStringFunction) {
            ((AsyncStringFunction) this.function).apply(getOnlyValue(routingContext), callback::accept, errorHandler(routingContext));
        }
        else if (function instanceof AsyncSupplier) {
            ((AsyncSupplier) function).get(callback::accept, errorHandler(routingContext));
        }
        else if (function instanceof AsyncIntFunction) {
            ((AsyncIntFunction) function).apply(Integer.parseInt(getOnlyValue(routingContext)), callback::accept, errorHandler(routingContext));
        }
        else {
            throw new UnsupportedOperationException(String.format("Async controller with this arity is not yet implemented: [%s]", function.getClass().getName()));
        }
    }

    private void executeBlockingFunction(RoutingContext routingContext, Consumer<Object> callback) {
        try {
            Object value;
            if (function instanceof StringFunction) {
                value = ((StringFunction) function).apply(getOnlyValue(routingContext));
            } else if (function instanceof Supplier) {
                value = ((Supplier) function).get();
            } else if (function instanceof IntFunction) {
                value = ((IntFunction) function).apply(Integer.parseInt(getOnlyValue(routingContext)));
            } else {
                throw new UnsupportedOperationException(String.format("Controller with this arity is not yet implemented: [%s]", function.getClass().getName()));
            }
            callback.accept(value);
        } catch (Exception throwable) {
            LOGGER.error(String.format("Invoking controller method failed: [%s]", routingContext.currentRoute().getPath()), throwable);
            routingContext.fail(500);
        }
    }

    private static Consumer<Throwable> errorHandler(RoutingContext routingContext) {
        return throwable -> {
            String path = routingContext.currentRoute() != null ? routingContext.currentRoute().getPath() : null;
            LOGGER.error(String.format("Invoking controller method failed: [%s]", path), throwable);
            routingContext.fail(500);
        };
    }

}
