package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

@FunctionalInterface
public interface AsyncDoubleFunction<T> extends Async {

    void apply(Double value, AsyncResultHandler<T> handler);

}
