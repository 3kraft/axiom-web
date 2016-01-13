package org.zalando.axiom.web.krueger.metrics

interface AppMetricsSupplier {

    fun supply(): Map<String, Number>

}
