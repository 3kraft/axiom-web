package org.zalando.axiom.web.binding;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;
import static org.zalando.axiom.web.util.Strings.toSwaggerPathParams;
import static org.zalando.axiom.web.util.Strings.toVertxPathParams;

class RouteConfiguration {

    private final String vertxPath;

    private final String swaggerPath;

    private final Map<HttpMethod, Handler<RoutingContext>> httpMethodToHandler;

    RouteConfiguration(String path) {
        checkNotNull(path, "Path must not be null!");
        this.vertxPath = toVertxPathParams(path);
        this.swaggerPath = toSwaggerPathParams(path);
        this.httpMethodToHandler = new HashMap<>();
    }

    void addHandler(HttpMethod httpMethod, Handler<RoutingContext> handler) {
        if (httpMethodToHandler.containsKey(httpMethod)) {
            throw new IllegalStateException(String.format("Route [%s] already contains handler for method [%s]!", vertxPath, httpMethod));
        }
        httpMethodToHandler.put(httpMethod, handler);
    }

    String getVertxPath() {
        return vertxPath;
    }

    public String getSwaggerPath() {
        return swaggerPath;
    }

    Set<Map.Entry<HttpMethod, Handler<RoutingContext>>> entrySet() {
        return httpMethodToHandler.entrySet();
    }
}
