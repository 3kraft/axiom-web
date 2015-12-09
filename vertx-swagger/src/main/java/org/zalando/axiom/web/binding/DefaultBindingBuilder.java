package org.zalando.axiom.web.binding;

import io.vertx.core.http.HttpMethod;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.handler.Get2Handler;

import java.util.function.Function;

public class DefaultBindingBuilder implements BindingBuilder {

    private final SwaggerRouter swaggerRouter;

    private final String path;

    public DefaultBindingBuilder(SwaggerRouter swaggerRouter, String path) {
        this.swaggerRouter = swaggerRouter;
        this.path = path;
    }

    public <T, U> DefaultBindingBuilder get(Function<T, U> function) {
        swaggerRouter.route(HttpMethod.GET, path).handler(new Get2Handler(swaggerRouter.getMapper(), function));
        return this;
    }

    @Override
    public SwaggerRouter doBind() {
        return swaggerRouter;
    }
}
