package org.zalando.axiom.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.zalando.axiom.web.binding.DefaultBindingBuilder;
import org.zalando.axiom.web.exceptions.LoadException;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

public final class SwaggerRouter implements Router {

    private final Router router;
    private ObjectMapper mapper;
    private PropertyNamingStrategy propertyNamingStrategy = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;

    private SwaggerRouter(Router router) {
        this.router = router;
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(
                this.propertyNamingStrategy);
    }

    public static SwaggerRouter router(Vertx vertx) {
        Router router = Router.router(vertx);
        router.post().handler(BodyHandler.create());
        return new SwaggerRouter(router);
    }

    public SwaggerRouter propertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
        return this;
    }

    public SwaggerRouter mapper(ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }


    public DefaultBindingBuilder bindTo(String path) {
        return new DefaultBindingBuilder(this, path);
    }

    @Override
    public void accept(HttpServerRequest request) {
        router.accept(request);
    }

    @Override
    public Route route() {
        return router.route();
    }

    @Override
    public Route route(HttpMethod method, String path) {
        return router.route(method, path);
    }

    @Override
    public Route route(String path) {
        return router.route(path);
    }

    @Override
    public Route routeWithRegex(HttpMethod method, String regex) {
        return router.routeWithRegex(method, regex);
    }

    @Override
    public Route routeWithRegex(String regex) {
        return router.routeWithRegex(regex);
    }

    @Override
    public Route get() {
        return router.get();
    }

    @Override
    public Route get(String path) {
        return router.get(path);
    }

    @Override
    public Route getWithRegex(String regex) {
        return router.getWithRegex(regex);
    }

    @Override
    public Route head() {
        return router.head();
    }

    @Override
    public Route head(String path) {
        return router.head(path);
    }

    @Override
    public Route headWithRegex(String regex) {
        return router.headWithRegex(regex);
    }

    @Override
    public Route options() {
        return router.options();
    }

    @Override
    public Route options(String path) {
        return router.options(path);
    }

    @Override
    public Route optionsWithRegex(String regex) {
        return router.optionsWithRegex(regex);
    }

    @Override
    public Route put() {
        return router.put();
    }

    @Override
    public Route put(String path) {
        return router.put(path);
    }

    @Override
    public Route putWithRegex(String regex) {
        return router.putWithRegex(regex);
    }

    @Override
    public Route post() {
        return router.post();
    }

    @Override
    public Route post(String path) {
        return router.post(path);
    }

    @Override
    public Route postWithRegex(String regex) {
        return router.postWithRegex(regex);
    }

    @Override
    public Route delete() {
        return router.delete();
    }

    @Override
    public Route delete(String path) {
        return router.delete(path);
    }

    @Override
    public Route deleteWithRegex(String regex) {
        return router.deleteWithRegex(regex);
    }

    @Override
    public Route trace() {
        return router.trace();
    }

    @Override
    public Route trace(String path) {
        return router.trace(path);
    }

    @Override
    public Route traceWithRegex(String regex) {
        return router.traceWithRegex(regex);
    }

    @Override
    public Route connect() {
        return router.connect();
    }

    @Override
    public Route connect(String path) {
        return router.connect(path);
    }

    @Override
    public Route connectWithRegex(String regex) {
        return router.connectWithRegex(regex);
    }

    @Override
    public Route patch() {
        return router.patch();
    }

    @Override
    public Route patch(String path) {
        return router.patch(path);
    }

    @Override
    public Route patchWithRegex(String regex) {
        return router.patchWithRegex(regex);
    }

    @Override
    public List<Route> getRoutes() {
        return router.getRoutes();
    }

    @Override
    public Router clear() {
        return router.clear();
    }

    @Override
    public Router mountSubRouter(String mountPoint, Router subRouter) {
        return router.mountSubRouter(mountPoint, subRouter);
    }

    @Override
    public Router exceptionHandler(Handler<Throwable> exceptionHandler) {
        return router.exceptionHandler(exceptionHandler);
    }

    @Override
    public void handleContext(RoutingContext context) {
        router.handleContext(context);
    }

    @Override
    public void handleFailure(RoutingContext context) {
        router.handleFailure(context);
    }

    private Swagger load(InputStream jsonStream) {
        try (InputStreamReader reader = new InputStreamReader(jsonStream); Reader bufferedReader = new BufferedReader(reader)) {
            return new Swagger20Parser().read(new ObjectMapper().readTree(bufferedReader));
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }

    private Swagger load(java.nio.file.Path jsonPath) {
        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            return new Swagger20Parser().read(new ObjectMapper().readTree(reader));
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }
}

