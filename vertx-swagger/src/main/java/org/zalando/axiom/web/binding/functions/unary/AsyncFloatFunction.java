package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

@FunctionalInterface
public interface AsyncFloatFunction<T> extends Async {

    void apply(Float value, AsyncResultHandler<T> handler);

}
