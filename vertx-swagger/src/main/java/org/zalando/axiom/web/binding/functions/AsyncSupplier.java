package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

public interface AsyncSupplier<T> extends Async {

    void get(AsyncResultHandler<T> handler);

}
