package org.zalando.axiom.web.binding.functions;

import java.util.function.Consumer;

public interface AsyncStringFunction<T> extends Async {

    void apply(String value, Consumer<T> callback, Consumer<Throwable> errorHandler);

}
