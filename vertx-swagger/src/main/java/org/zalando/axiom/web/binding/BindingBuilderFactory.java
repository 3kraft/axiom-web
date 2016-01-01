package org.zalando.axiom.web.binding;

import io.swagger.models.Swagger;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.StringFunction;
import org.zalando.axiom.web.util.Strings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;

public class BindingBuilderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(BindingBuilderFactory.class);

    private final SwaggerRouter swaggerRouter;

    private final Map<String, RouteConfiguration> routeConfigurations;

    public BindingBuilderFactory(SwaggerRouter swaggerRouter) {
        this.swaggerRouter = swaggerRouter;
        this.routeConfigurations = new HashMap<>();
    }

    public DefaultBindingBuilder bindTo(String path) {
        checkNotNull(path, "Path must not be null!");
        return new DefaultBindingBuilder(this, swaggerRouter, path);
    }

    public BindingBuilderFactory get(String path, Supplier<Object> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T, R> BindingBuilderFactory get(String path, Class<T> paramType, Function<T, R> function) {
        getBindingBuilder(path).get(paramType, function).doBind();
        return this;
    }

    public BindingBuilderFactory getById(String path, StringFunction<Object> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public Router router(Vertx vertx) {
        SwaggerVertxRouter router = SwaggerVertxRouter.router(vertx);
        for (RouteConfiguration routeConfiguration : routeConfigurations.values()) {
            for (Map.Entry<HttpMethod, Handler<RoutingContext>> entry : routeConfiguration.entrySet()) {
                final Swagger swagger = swaggerRouter.getSwagger();

                final String basePath = Strings.valueOrElse(swagger.getBasePath(), "");
                String path = basePath + routeConfiguration.getVertxPath();

                LOGGER.debug(String.format("Binding method [%s] and path [%s].", entry.getKey(), path));
                router.route(entry.getKey(), path).handler(entry.getValue());
            }
        }
        return router;
    }

    private DefaultBindingBuilder getBindingBuilder(String path) {
        checkNotNull(path, "Path must not be null!");
        return new DefaultBindingBuilder(this, swaggerRouter, path);
    }

    void registerRoute(RouteConfiguration routeConfiguration) {
        String swaggerPath = routeConfiguration.getSwaggerPath();
        if (routeConfigurations.containsKey(swaggerPath)) {
            throw new IllegalStateException(String.format("Configuration for route [%s] is already added!", swaggerPath));
        }
        routeConfigurations.put(swaggerPath, routeConfiguration);
    }
}
