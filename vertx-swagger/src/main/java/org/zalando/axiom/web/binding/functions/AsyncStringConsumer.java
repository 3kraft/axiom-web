package org.zalando.axiom.web.binding.functions;

import io.vertx.core.AsyncResultHandler;

@FunctionalInterface
public interface AsyncStringConsumer extends AsyncConsumer<String> {

    void accept(String object, AsyncResultHandler<Void> handler);

}
