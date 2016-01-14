package org.zalando.axiom.web.util;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;
import org.zalando.axiom.web.handler.TestCoordinator;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class VertxUtils {

    public static TestCoordinator setUpDeleteRequest(Vertx vertx, TestContext context, Async async, String uriWithParameters, int expectedStatusCode) {
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.delete(8080, "127.0.0.1", uriWithParameters);
        TestCoordinator coordinator = new TestCoordinator(request, async);

        request.handler(response -> {
            checkStatusCode(context, async, expectedStatusCode, response);
            coordinator.stopServer();
        });
        failHandler(context, coordinator, request);
        return coordinator;
    }

    public static TestCoordinator setUpGetRequest(Vertx vertx, TestContext context, Async async, String uriWithParameters, int expectedStatusCode, Consumer<HttpClientResponse> tester) {
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.get(8080, "127.0.0.1", uriWithParameters);
        TestCoordinator coordinator = new TestCoordinator(request, async);

        request.handler(response -> {
            checkStatusCode(context, async, expectedStatusCode, response);
            tester.accept(response);
            coordinator.stopServer();
        });
        failHandler(context, coordinator, request);
        return coordinator;
    }

    public static TestCoordinator setUpPostRequest(Vertx vertx, TestContext context, Async async, String uri, int expectedStatusCode, Consumer<HttpClientResponse> tester) {
        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.post(8080, "127.0.0.1", uri);
        TestCoordinator coordinator = new TestCoordinator(request, async);

        request.handler(response -> {
                    checkStatusCode(context, async, expectedStatusCode, response);
                    tester.accept(response);
                    coordinator.stopServer();
                }
        );
        failHandler(context, coordinator, request);
        return coordinator;
    }

    public static void startHttpServer(Vertx vertx, TestCoordinator coordinator, Supplier<Router> routerFactory) {
        startHttpServer(vertx, coordinator, null, null, routerFactory);
    }

    public static void startHttpServer(Vertx vertx, TestCoordinator coordinator, String requestBody, Class<? extends Exception> expectedException, Supplier<Router> routerFactory) {
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start(Future<Void> startFuture) throws Exception {
                HttpServer server = vertx.createHttpServer();
                try {
                    Router router = routerFactory.get();
                    server.requestHandler(router::accept).listen(8080, handler -> {
                        if (handler.succeeded()) {
                            if (requestBody == null) {
                                coordinator.startRequest(server);
                            } else {
                                coordinator.startRequest(server, requestBody);
                            }
                            startFuture.complete();
                        } else {
                            throw new IllegalStateException("Server did not start!", handler.cause());
                        }
                    });
                } catch (Exception e) {
                    coordinator.getAsync().complete();
                    if (expectedException != null && !e.getClass().equals(expectedException)) {
                        throw e;
                    }
                }

            }
        });
    }

    private static void checkStatusCode(TestContext context, Async async, int expectedStatusCode, HttpClientResponse response) {
        if (response.statusCode() != expectedStatusCode) {
            context.fail(String.format("Status code is [%d]", response.statusCode()));
            async.complete();
        }
    }

    private static void failHandler(TestContext context, TestCoordinator coordinator, HttpClientRequest request) {
        request.exceptionHandler(exception -> {
            context.fail(exception.getLocalizedMessage());
            coordinator.stopServer();
        });
    }
}
