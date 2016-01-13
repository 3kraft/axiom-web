package org.zalando.axiom.web.krueger

import com.codahale.metrics.Counter
import com.codahale.metrics.MetricRegistry
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.dropwizard.MetricsService
import java.util.*

class VertxMetricsService(val vertx: Vertx, val metrics: MetricRegistry = MetricRegistry()) {

    val metricsService: MetricsService = MetricsService.create(vertx)

    fun getVertxMetrics(): List<JsonObject> = listOf(metricsService.getMetricsSnapshot(vertx))

    fun getStatusCodesMetrics(): Map<String, Long> {
        val result = HashMap<String, Counter>()
        metrics.counters.filterTo(result) { it.key.startsWith("statusCodes.") }
        return result.mapValues { it.value.count };
    }

    fun getEventBusMetrics(): Map<String, Number> {
        val values = sortedMapOf<String, Number>()
        val metricsSnapshot = metricsService.getMetricsSnapshot(vertx.eventBus())
        for ((key, value) in metricsSnapshot) {
            if (key.startsWith("handlers") && key != "handlers") {
                val handlerData = value as JsonObject
                for ((handlerKey, handlerValue) in handlerData) {
                    if (handlerValue is Number) {
                        val mapKey = handlerKey.replace(".", "").replace("%", "thPercentile")
                        values["$key.$mapKey"] = handlerValue
                    }
                }
            }
        }
        values["messages.pending"] = metricsSnapshot.getJsonObject("messages.pending").getInteger("count")
        return values
    }

}