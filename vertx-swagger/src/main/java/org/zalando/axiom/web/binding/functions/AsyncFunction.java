package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

public interface AsyncFunction<T, R> extends Async {

    void apply(T value, AsyncResultHandler<R> handler);

}
