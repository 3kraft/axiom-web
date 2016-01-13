package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

@FunctionalInterface
public interface AsyncIntFunction<T> extends Async {

    void apply(Integer value, AsyncResultHandler<T> handler);

}
