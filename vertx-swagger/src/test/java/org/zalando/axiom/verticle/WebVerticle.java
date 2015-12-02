package org.zalando.axiom.verticle;

import io.vertx.core.AbstractVerticle;
import org.zalando.axiom.SwaggerRouter;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class WebVerticle extends AbstractVerticle {

    final List controllers;

    final String jsonPath;

    public WebVerticle(String jsonPath, Object... controllers) {
        this.jsonPath = jsonPath;
        this.controllers = Arrays.asList(controllers);
    }

    @Override
    public void start() throws Exception {
        InputStream jsonStream = this.getClass().getResourceAsStream(jsonPath);

        try {
            SwaggerRouter router = SwaggerRouter.router(vertx);
            controllers.forEach(controller -> router.controller(controller));
            router.setupRoutes(jsonStream);
            vertx.createHttpServer().requestHandler(requestHandler -> router.accept(requestHandler)).listen(8080);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
