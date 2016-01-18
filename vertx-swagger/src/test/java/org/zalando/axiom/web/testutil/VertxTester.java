package org.zalando.axiom.web.testutil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

import java.util.function.Supplier;

public class VertxTester {

    private final TestContext testContext;

    private Async async;

    private boolean requestStarted = false;

    private HttpClientRequest request;

    private HttpServer server;

    private Supplier<Router> routerFactory;

    private int expectedStatusCode = 200;

    private ResponseConsumer responseAssertion;

    private String getRequestUri;

    VertxTester(TestContext testContext) {
        this.testContext = testContext;
    }

    VertxTester routerFactory(Supplier<Router> routerFactory) {
        this.routerFactory = routerFactory;
        return this;
    }

    VertxTester expectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
        return this;
    }

    VertxTester responseAssertion(ResponseConsumer responseAssertion) {
        this.responseAssertion = responseAssertion;
        return this;
    }

    VertxTester getRequest(String uri) {
        this.getRequestUri = uri;
        return this;
    }

    void checkStatusCode(HttpClientResponse response) {
        if (response.statusCode() != expectedStatusCode) {
            testContext.fail(String.format("Status code is [%d] but expected is [%d]!", response.statusCode(), expectedStatusCode));
            stopServer();
        }
    }

    void failHandler(HttpClientRequest request) {
        request.exceptionHandler(exception -> {
            testContext.fail(exception.getLocalizedMessage());
            stopServer();
        });
    }

    void stopServer() {
        if (!requestStarted) {
            testContext.fail("Request was not fired, and server stopped!");
        }
        if (server == null) {
            testContext.fail("Could not stop server as instance is null!");
        }
        server.close(event -> async.complete());
    }

    void start(Vertx vertx, String requestBody) {
        this.async = testContext.async();

        this.request = vertx.createHttpClient().get(8080, "127.0.0.1", getRequestUri);

        request.handler(response -> {
            checkStatusCode(response);
            try {
                if (responseAssertion != null) {
                    responseAssertion.accept(testContext, response);
                }
            } catch (Exception e) {
                testContext.fail(e);
            } finally {
                stopServer();
            }
        });
        failHandler(request);

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start(Future<Void> startFuture) throws Exception {
                server = vertx.createHttpServer();
                try {
                    if (routerFactory == null) {
                        testContext.fail("A router factory must be specified!");
                    }
                    Router router = routerFactory.get();
                    server.requestHandler(router::accept).listen(8080, handler -> {
                        if (handler.succeeded()) {
                            if (requestBody == null) {
                                request.end();
                            } else {
                                request.end(requestBody);
                            }
                            requestStarted = true;
                            startFuture.complete();
                        } else {
                            throw new IllegalStateException("Server did not start!", handler.cause());
                        }
                    });
                } catch (Exception e) {
                    testContext.fail(e);
                    stopServer();
//                    if (expectedException != null && !e.getClass().equals(expectedException)) {
//                        throw e;
//                    }
                }
            }
        });
    }

}
