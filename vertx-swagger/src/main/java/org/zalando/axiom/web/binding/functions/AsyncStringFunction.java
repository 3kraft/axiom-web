package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

public interface AsyncStringFunction<T> extends Async {

    void apply(String value, AsyncResultHandler<T> handler);

}
