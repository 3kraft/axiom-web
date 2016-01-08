package org.zalando.axiom.web.binding.functions;

import java.util.function.Consumer;

public interface AsyncSupplier<T> extends Async {

    void get(Consumer<T> callback, Consumer<Throwable> errorHandler);

}
