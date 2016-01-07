package org.zalando.axiom.web.binding.functions;

import java.util.function.Consumer;

public interface AsyncConsumer<T, R> extends Async {

    void accept(T object, Consumer<R> callback);

}
