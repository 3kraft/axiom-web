package org.zalando.axiom.web.krueger

import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.Vertx

fun <T, R> List<Map<T, R>>.flatten() : Map<T, R> = this.flatMap { e -> e.entries }.associateBy({ e -> e.key }, { e -> e.value })

fun <T> Vertx.executeBlocking(handler: AsyncResultHandler<T>, callback: () -> T) {
    executeBlocking({ future: Future<T> ->
        future.complete(callback())
    }, { result ->
        if (result.succeeded()) {
            handler.handle(Future.succeededFuture(result.result()))
        } else {
            handler.handle(Future.failedFuture(result.cause()))
        }
    })
}