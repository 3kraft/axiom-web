package org.zalando.axiom.web.binding;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.models.Operation;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.*;
import org.zalando.axiom.web.handler.*;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class DefaultBindingBuilder implements BindingBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBindingBuilder.class);

    private final BindingBuilderFactory bindingBuilderFactory;

    private final SwaggerRouter swaggerRouter;

    private final RouteConfiguration routeConfiguration;

    private final Map<io.swagger.models.HttpMethod, Operation> operationMap;

    public DefaultBindingBuilder(BindingBuilderFactory bindingBuilderFactory, SwaggerRouter swaggerRouter, String path) {
        this.bindingBuilderFactory = bindingBuilderFactory;
        this.swaggerRouter = swaggerRouter;
        this.routeConfiguration = new RouteConfiguration(path);
        this.operationMap = swaggerRouter.getSwagger().getPath(routeConfiguration.getSwaggerPath()).getOperationMap();
    }

    public <T> DefaultBindingBuilder get(Supplier<T> function) {
        get((Object) function);
        return this;
    }

    public <T> DefaultBindingBuilder get(StringFunction<T> function) {
        get((Object) function);
        return this;
    }

    public <T> DefaultBindingBuilder get(IntFunction<T> function) {
        get((Object) function);
        return this;
    }

    public <T> DefaultBindingBuilder get(AsyncStringFunction<T> function) {
        get((Object) function);
        return this;
    }

    public <T> DefaultBindingBuilder get(AsyncIntFunction<T> function) {
        get((Object) function);
        return this;
    }

    public <T> DefaultBindingBuilder get(AsyncSupplier<T> function) {
        get((Object) function);
        return this;
    }

    public <T, R> DefaultBindingBuilder get(Class<T> paramType, Function<T, R> function) {
        routeConfiguration.addHandler(HttpMethod.GET, new ParameterCheckHandler(operationMap.get(io.swagger.models.HttpMethod.GET)));
        routeConfiguration.addHandler(HttpMethod.GET, toMetricsHandler(new GetHandler<>(swaggerRouter.getMapper(), function, paramType, swaggerRouter.getSwagger().getPath(routeConfiguration.getSwaggerPath()))));
        return this;
    }

    public <T> DefaultBindingBuilder getMetrics(Function<MetricRegistry, T> function) {
        routeConfiguration.addHandler(HttpMethod.GET, routingContext -> {
            try {
                routingContext.response().end(swaggerRouter.getMapper().writeValueAsString(function.apply(swaggerRouter.getMetricsRegistry())));
            } catch (JsonProcessingException e) {
                LOGGER.error("Error on de-serializing metrics!", e);
                routingContext.response().setStatusCode(500).end();
            }
        });
        return this;
    }

    private DefaultBindingBuilder get(Object function) {
        routeConfiguration.addHandler(HttpMethod.GET, new ParameterCheckHandler(operationMap.get(io.swagger.models.HttpMethod.GET)));
        routeConfiguration.addHandler(HttpMethod.GET, new ParameterCheckHandler(operationMap.get(io.swagger.models.HttpMethod.GET)));
        routeConfiguration.addHandler(HttpMethod.GET, toMetricsHandler(new GetWithZeroOrOneParameterHandler(swaggerRouter.getMapper(), function)));
        return this;
    }

    public <T, R> DefaultBindingBuilder post(Class<T> paramType, Function<T, R> function) {
        Operation postOperation = swaggerRouter.getSwagger().getPath(routeConfiguration.getSwaggerPath()).getPost();
        routeConfiguration.addHandler(HttpMethod.POST, toMetricsHandler(new PostHandler<>(postOperation, swaggerRouter.getMapper(), function, paramType)));
        return this;
    }

    public <T, R> DefaultBindingBuilder post(Class<T> paramType, AsyncConsumer<T, R> function) {
        Operation postOperation = swaggerRouter.getSwagger().getPath(routeConfiguration.getSwaggerPath()).getPost();
        routeConfiguration.addHandler(HttpMethod.POST, toMetricsHandler(new PostHandler<>(postOperation, swaggerRouter.getMapper(), function, paramType)));
        return this;
    }

    public DefaultBindingBuilder delete(Consumer<String> function) {
        routeConfiguration.addHandler(HttpMethod.DELETE, toMetricsHandler(new DeleteHandler(function)));
        return this;
    }

    @Override
    public BindingBuilderFactory doBind() {
        bindingBuilderFactory.registerRoute(routeConfiguration);
        return bindingBuilderFactory;
    } // TODO validation

    private <T, R> Handler<RoutingContext> toMetricsHandler(Handler<RoutingContext> handler) {
        if (swaggerRouter.isMetricsEnabled()) {
            return new MetricsHandler(swaggerRouter.getMetricsRegistry(), handler, routeConfiguration.getVertxPath());
        } else {
            return handler;
        }
    }
}
