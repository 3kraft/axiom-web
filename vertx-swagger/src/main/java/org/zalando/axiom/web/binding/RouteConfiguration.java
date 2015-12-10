package org.zalando.axiom.web.binding;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;

class RouteConfiguration {

    private final String path;

    private final Map<HttpMethod, Handler<RoutingContext>> httpMethodToHandler;

    RouteConfiguration(String path) {
        checkNotNull(path, "Path must not be null!");
        this.path = path;
        this.httpMethodToHandler = new HashMap<>();
    }

    void addHandler(HttpMethod httpMethod, Handler<RoutingContext> handler) {
        if (httpMethodToHandler.containsKey(httpMethod)) {
            throw new IllegalStateException(String.format("Route [%s] already contains handler for method [%s]!", path, httpMethod));
        }
        httpMethodToHandler.put(httpMethod, handler);
    }

    String getPath() {
        return path;
    }

    Set<Map.Entry<HttpMethod, Handler<RoutingContext>>> entrySet() {
        return httpMethodToHandler.entrySet();
    }
}
