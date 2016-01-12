package org.zalando.axiom.web.binding.functions;

import java.util.function.Consumer;

public interface AsyncIntFunction<T> extends Async {

    void apply(Integer value, Consumer<T> callback);

}
