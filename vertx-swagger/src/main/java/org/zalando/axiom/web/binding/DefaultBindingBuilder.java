package org.zalando.axiom.web.binding;

import io.vertx.core.http.HttpMethod;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.StringFunction;
import org.zalando.axiom.web.handler.GetHandler;
import org.zalando.axiom.web.handler.GetWithZeroOrOneParameterHandler;
import org.zalando.axiom.web.handler.PostHandler;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class DefaultBindingBuilder implements BindingBuilder {

    private final BindingBuilderFactory bindingBuilderFactory;

    private final SwaggerRouter swaggerRouter;

    private final RouteConfiguration routeConfiguration;

    public DefaultBindingBuilder(BindingBuilderFactory bindingBuilderFactory, SwaggerRouter swaggerRouter, String path) {
        this.bindingBuilderFactory = bindingBuilderFactory;
        this.swaggerRouter = swaggerRouter;
        this.routeConfiguration = new RouteConfiguration(path);
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

    public <T, R> DefaultBindingBuilder get(Class<T> paramType, Function<T, R> function) {
        routeConfiguration.addHandler(HttpMethod.GET, new GetHandler<>(swaggerRouter.getMapper(), function, paramType, swaggerRouter.getSwagger().getPath(routeConfiguration.getPath())));
        return this;
    }


    private DefaultBindingBuilder get(Object function) {
        routeConfiguration.addHandler(HttpMethod.GET, new GetWithZeroOrOneParameterHandler(swaggerRouter.getMapper(), function));
        return this;
    }

    public <T, R> DefaultBindingBuilder post(Class<T> paramType, Function<T, R> function) {
        routeConfiguration.addHandler(HttpMethod.POST, new PostHandler<>(swaggerRouter.getMapper(), function, paramType));
        return this;
    }

    @Override
    public BindingBuilderFactory doBind() {
        bindingBuilderFactory.registerRoute(routeConfiguration);
        return bindingBuilderFactory;
    } // TODO validation

}
