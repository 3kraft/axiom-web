package org.zalando.axiom.web.binding.functions;

import java.util.function.Consumer;

public interface AsyncFunction<T, R> extends Async {

    void apply(T value, Consumer<R> callback);

}
