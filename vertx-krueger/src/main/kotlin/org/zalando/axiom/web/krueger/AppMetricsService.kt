package org.zalando.axiom.web.krueger

import io.vertx.core.AsyncResultHandler
import io.vertx.core.Vertx
import org.zalando.axiom.web.krueger.metrics.AppMetricsSupplier

class AppMetricsService(val vertx: Vertx, val metricsSuppliers: List<AppMetricsSupplier>) {

    fun getMetrics(handler: AsyncResultHandler<Map<String, Number>>) {
        vertx.executeBlocking(handler, {
            metricsSuppliers.map { metricsSupplier -> metricsSupplier.supply() }.flatten().toSortedMap()
        })
    }

}
