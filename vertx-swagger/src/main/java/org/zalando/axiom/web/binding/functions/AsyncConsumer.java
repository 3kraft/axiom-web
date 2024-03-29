package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

@FunctionalInterface
public interface AsyncConsumer<T> extends Async {

    void accept(T object, AsyncResultHandler<Void> handler);

}
