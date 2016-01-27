package org.zalando.axiom.web.krueger

import com.codahale.metrics.Counter
import com.codahale.metrics.MetricRegistry
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.dropwizard.MetricsService
import java.util.*

class VertxMetricsService(val vertx: Vertx, val metrics: MetricRegistry = MetricRegistry()) {

    val metricsService: MetricsService = MetricsService.create(vertx)

    fun getVertxMetrics(handler: AsyncResultHandler<List<JsonObject>>) = vertx.executeBlocking(handler) {
        listOf(metricsService.getMetricsSnapshot(vertx))
    }

    fun getStatusCodesMetrics(handler: AsyncResultHandler<Map<String, Long>>) {
        vertx.executeBlocking(handler) {
            val result = HashMap<String, Counter>()
            metrics.counters.filterTo(result) { it.key.startsWith("statusCodes.") }
            result.mapValues { it.value.count }
        }
    }

    fun getEventBusMetrics(handler: AsyncResultHandler<Map<String, Number>>) {
        vertx.executeBlocking(handler) {
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
            values
        }
    }

}