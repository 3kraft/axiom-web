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
import org.zalando.axiom.web.binding.functions.AsyncIntFunction;
import org.zalando.axiom.web.binding.functions.AsyncStringFunction;
import org.zalando.axiom.web.binding.functions.AsyncSupplier;
import org.zalando.axiom.web.binding.functions.StringFunction;
import org.zalando.axiom.web.util.Strings;

import java.util.HashMap;
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
        checkPathBinding(path);
        return new DefaultBindingBuilder(this, swaggerRouter, path);
    }

    public <T> BindingBuilderFactory get(String path, Supplier<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T> BindingBuilderFactory get(String path, AsyncSupplier<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T, R> BindingBuilderFactory get(String path, Class<T> paramType, Function<T, R> function) {
        getBindingBuilder(path).get(paramType, function).doBind();
        return this;
    }

    public <T> BindingBuilderFactory get(String path, StringFunction<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T> BindingBuilderFactory get(String path, AsyncStringFunction<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T> BindingBuilderFactory get(String path, AsyncIntFunction<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T> BindingBuilderFactory getById(String path, StringFunction<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T> BindingBuilderFactory getById(String path, AsyncStringFunction<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public <T> BindingBuilderFactory getById(String path, AsyncIntFunction<T> supplier) {
        getBindingBuilder(path).get(supplier).doBind();
        return this;
    }

    public Router router(Vertx vertx) {
        SwaggerVertxRouter router = SwaggerVertxRouter.router(vertx);
        for (RouteConfiguration routeConfiguration : routeConfigurations.values()) {
            for (Map.Entry<HttpMethod, List<Handler<RoutingContext>>> entry : routeConfiguration.entrySet()) {
                final Swagger swagger = swaggerRouter.getSwagger();

                final String basePath = Strings.valueOrElse(swagger.getBasePath(), "");
                String path = basePath + routeConfiguration.getVertxPath();

                LOGGER.debug(String.format("Binding method [%s] and path [%s].", entry.getKey(), path));
                entry.getValue().forEach(handler -> router.route(entry.getKey(), path).handler(handler));
            }
        }
        return router;
    }

    private DefaultBindingBuilder getBindingBuilder(String path) {
        checkNotNull(path, "Path must not be null!");
        checkPathBinding(path);
        return new DefaultBindingBuilder(this, swaggerRouter, path);
    }

    void checkPathBinding(String path) {
        if (routeConfigurations.containsKey(path)) {
            throw new IllegalStateException(String.format("Configuration for route [%s] is already added!", path));
        }
    }

    void registerRoute(RouteConfiguration routeConfiguration) {
        routeConfigurations.put(routeConfiguration.getSwaggerPath(), routeConfiguration);
    }
}
