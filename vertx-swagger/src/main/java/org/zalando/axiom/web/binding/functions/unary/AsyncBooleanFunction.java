package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

@FunctionalInterface
public interface AsyncBooleanFunction<T> extends Async {

    void apply(Boolean value, AsyncResultHandler<T> handler);

}
