package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

@FunctionalInterface
public interface AsyncLongFunction<T> extends Async {

    void apply(Long value, AsyncResultHandler<T> handler);

}
