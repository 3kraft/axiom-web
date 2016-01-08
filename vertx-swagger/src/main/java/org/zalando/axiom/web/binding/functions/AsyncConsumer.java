package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

public interface AsyncConsumer<T, R> extends Async {

    void accept(T object, AsyncResultHandler<R> handler);

}
