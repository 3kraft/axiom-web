package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

@FunctionalInterface
public interface AsyncIntFunction<T> extends Async {

    void apply(Integer value, AsyncResultHandler<T> handler);

}
