package org.zalando.axiom.web.binding.functions.unary;

import io.vertx.core.AsyncResultHandler;
import org.zalando.axiom.web.binding.functions.Async;

import java.util.Date;

@FunctionalInterface
public interface AsyncDateFunction<T> extends Async {

    void apply(Date value, AsyncResultHandler<T> handler);

}
