package org.zalando.axiom.web.binding;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.util.Util;

import java.util.*;

import static org.zalando.axiom.web.util.Preconditions.checkNotNull;
import static org.zalando.axiom.web.util.Strings.toSwaggerPathParams;
import static org.zalando.axiom.web.util.Strings.toVertxPathParams;
import static org.zalando.axiom.web.util.Util.getOrPut;

class RouteConfiguration {

    private final String vertxPath;

    private final String swaggerPath;

    private final Map<HttpMethod, List<Handler<RoutingContext>>> httpMethodToHandler;

    RouteConfiguration(String path) {
        checkNotNull(path, "Path must not be null!");
        this.vertxPath = toVertxPathParams(path);
        this.swaggerPath = toSwaggerPathParams(path);
        this.httpMethodToHandler = new HashMap<>();
    }

    void addHandler(HttpMethod httpMethod, Handler<RoutingContext> handler) {
        getOrPut(httpMethodToHandler, httpMethod, LinkedList<Handler<RoutingContext>>::new).add(handler);
    }

    String getVertxPath() {
        return vertxPath;
    }

    public String getSwaggerPath() {
        return swaggerPath;
    }

    Set<Map.Entry<HttpMethod, List<Handler<RoutingContext>>>> entrySet() {
        return httpMethodToHandler.entrySet();
    }
}
