package org.zalando.axiom.web.handler;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;

public class TestCoordinator {

    private final HttpClientRequest request;

    private final Async async;

    private HttpServer server;

    public TestCoordinator(HttpClientRequest request, Async async) {
        this.request = request;
        this.async = async;
    }

    public void startRequest(HttpServer server) {
        this.server = server;
        request.end();
    }

    public void startRequest(HttpServer server, String requestBody) {
        this.server = server;
        request.end(requestBody);
    }

    public void stopServer() {
        if (server == null) {
            throw new IllegalStateException("Could not stop server as instance is null!");
        }
        server.close(event -> async.complete());
    }

    public Async getAsync() {
        return async;
    }
}
