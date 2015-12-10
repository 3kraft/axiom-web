package org.zalando.axiom.web.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VertxUtils {

    public static HttpClientRequest setUpDeleteRequest(Vertx vertx, TestContext context, Async async, String uriWithParameters, int expectedStatusCode) {
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.delete(8080, "127.0.0.1", uriWithParameters);
        request.handler(response -> {
            checkStatusCode(context, async, expectedStatusCode, response);
            async.complete();
        });
        failHandler(context, async, request);
        return request;
    }

    public static HttpClientRequest setUpGetRequest(Vertx vertx, TestContext context, Async async, String uriWithParameters, int expectedStatusCode, Consumer<HttpClientResponse> tester) {
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.get(8080, "127.0.0.1", uriWithParameters);
        request.handler(response -> {
            checkStatusCode(context, async, expectedStatusCode, response);
            tester.accept(response);
            async.complete();
        });
        failHandler(context, async, request);
        return request;
    }

    public static HttpClientRequest setUpPostRequest(Vertx vertx, TestContext context, Async async, String uri, int expectedStatusCode, Consumer<HttpClientResponse> tester) {
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.post(8080, "127.0.0.1", uri, response -> {
            checkStatusCode(context, async, expectedStatusCode, response);
            tester.accept(response);
            async.complete();
        });
        failHandler(context, async, request);
        return request;
    }

    public static void startHttpServer(Vertx vertx, HttpClientRequest request, Supplier<Router> routerFactory) {
        startHttpServer(vertx, request, null, routerFactory);
    }

    public static void startHttpServer(Vertx vertx, HttpClientRequest request, String requestBody, Supplier<Router> routerFactory) {
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start(Future<Void> startFuture) throws Exception {
                vertx.createHttpServer().requestHandler(routerFactory.get()::accept).listen(8080, handler -> {
                    if (handler.succeeded()) {
                        if (requestBody == null) {
                            request.end();
                        } else {
                            request.end(requestBody);
                        }
                        startFuture.complete();
                    } else {
                        throw new IllegalStateException("Server did not start!", handler.cause());
                    }
                });
            }
        });
    }

    private static void checkStatusCode(TestContext context, Async async, int expectedStatusCode, HttpClientResponse response) {
        if (response.statusCode() != expectedStatusCode) {
            context.fail(String.format("Status code is [%d]", response.statusCode()));
            async.complete();
        }
    }

    private static void failHandler(TestContext context, Async async, HttpClientRequest request) {
        request.exceptionHandler(exception -> {
            context.fail(exception.getLocalizedMessage());
            async.complete();
        });
    }
}
