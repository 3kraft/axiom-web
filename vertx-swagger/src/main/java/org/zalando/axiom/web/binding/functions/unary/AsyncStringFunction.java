package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

@FunctionalInterface
public interface AsyncStringFunction<T> extends Async {

    void apply(String value, AsyncResultHandler<T> handler);

}
