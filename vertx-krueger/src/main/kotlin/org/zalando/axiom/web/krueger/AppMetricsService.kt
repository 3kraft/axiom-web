package org.zalando.axiom.web.krueger

import org.zalando.axiom.web.krueger.metrics.AppMetricsSupplier

class AppMetricsService(val metricsSuppliers: List<AppMetricsSupplier>) {

    fun getMetrics(): Map<String, Number> = metricsSuppliers.map { metricsSupplier -> metricsSupplier.supply() }.flatten().toSortedMap()

}
