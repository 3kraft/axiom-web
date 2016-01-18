package org.zalando.axiom.web.testutil;


import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.Router;

import java.util.function.Supplier;

public final class VertxTestBuilder {

    private Supplier<Router> routerFactory;

    private int expectedStatusCode = 200;

    private ResponseConsumer responseAssertion;

    private String getRequestUri;

    public VertxTestBuilder routerFactory(Supplier<Router> routerFactory) {
        this.routerFactory = routerFactory;
        return this;
    }

    public VertxTestBuilder expectedStatusCode(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
        return this;
    }

    public VertxTestBuilder responseAssertion(ResponseConsumer responseAssertion) {
        this.responseAssertion = responseAssertion;
        return this;
    }

    public VertxTestBuilder getRequest(String uri) {
        this.getRequestUri = uri;
        return this;
    }

    public void start(TestContext testContext, Vertx vertx) {
        start(testContext, vertx, null);
    }

    public void start(TestContext testContext, Vertx vertx, String requestBody) {
        new VertxTester(testContext)
                .expectedStatusCode(expectedStatusCode)
                .getRequest(getRequestUri)
                .routerFactory(routerFactory)
                .responseAssertion(responseAssertion)
                .start(vertx, requestBody);
    }

    public static VertxTestBuilder tester() {
        return new VertxTestBuilder();
    }
}
