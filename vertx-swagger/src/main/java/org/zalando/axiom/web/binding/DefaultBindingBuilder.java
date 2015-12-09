package org.zalando.axiom.web.binding;

import io.vertx.core.http.HttpMethod;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.handler.GetHandler;
import org.zalando.axiom.web.handler.PostHandler;

import java.util.function.Function;
import java.util.function.IntFunction;

import static org.zalando.axiom.web.util.Types.getParameterType;

public class DefaultBindingBuilder implements BindingBuilder {

    private final SwaggerRouter swaggerRouter;

    private final String path;

    public DefaultBindingBuilder(SwaggerRouter swaggerRouter, String path) {
        this.swaggerRouter = swaggerRouter;
        this.path = path;
    }

    public <T> DefaultBindingBuilder get(StringFunction<T> function) {
        get((Object) function);
        return this;
    }

    public <T> DefaultBindingBuilder get(IntFunction<T> function) {
        get((Object) function);
        return this;
    }

    private DefaultBindingBuilder get(Object function) {
        swaggerRouter.route(HttpMethod.GET, path).handler(new GetHandler(swaggerRouter.getMapper(), function));
        return this;
    }

    public <T, R> DefaultBindingBuilder post(Function<T, R> function, Class<T> paramType) {
        swaggerRouter.route(HttpMethod.POST, path).handler(new PostHandler<>(swaggerRouter.getMapper(), function, paramType));
        return this;
    }

    @Override
    public SwaggerRouter doBind() {
        return swaggerRouter;
    } // TODO validation

}
