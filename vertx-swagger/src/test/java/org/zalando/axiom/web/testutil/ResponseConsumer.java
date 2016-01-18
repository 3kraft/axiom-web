package org.zalando.axiom.web.testutil;

import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.TestContext;

@FunctionalInterface
public interface ResponseConsumer {

    void accept(TestContext context, HttpClientResponse response);

}
