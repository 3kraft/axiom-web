package org.zalando.axiom.web.binding;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.util.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;

public class BindingBuilderFactory {

    private final SwaggerRouter swaggerRouter;

    private final List<RouteConfiguration> routeConfigurations;

    public BindingBuilderFactory(SwaggerRouter swaggerRouter) {
        this.swaggerRouter = swaggerRouter;
        this.routeConfigurations = new LinkedList<>();
    }

    public DefaultBindingBuilder bindTo(String path) {
        checkNotNull(path, "Path must not be null!");
        return new DefaultBindingBuilder(this, swaggerRouter, path);
    }

    public Router router(Vertx vertx) {
        SwaggerVertxRouter router = SwaggerVertxRouter.router(vertx);
        for (RouteConfiguration routeConfiguration : routeConfigurations) {
            for (Map.Entry<HttpMethod, Handler<RoutingContext>> entry : routeConfiguration.entrySet()) {
                router.route(entry.getKey(), swaggerRouter.getSwagger().getBasePath() + routeConfiguration.getPath()).handler(entry.getValue());
            }
        }
        return router;
    }

    void registerRoute(RouteConfiguration routeConfiguration) {
        routeConfigurations.add(routeConfiguration);
    }
}
