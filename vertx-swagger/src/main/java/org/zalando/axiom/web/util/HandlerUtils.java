package org.zalando.axiom.web.util;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public final class HandlerUtils {

    private HandlerUtils() {
    }

    public static String getOnlyValue(RoutingContext routingContext) {
        MultiMap params = routingContext.request().params();
        if (params.size() != 1) {
            throw new IllegalStateException("Exactly one parameter required!");
        }
        String name = params.names().iterator().next();
        return params.get(name);
    }

}
