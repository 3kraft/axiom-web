package org.zalando.axiom.web.krueger.metrics

import com.codahale.metrics.MetricRegistry
import org.apache.commons.lang.StringUtils
import org.zalando.axiom.web.krueger.flatten


public class ZmonMetricsSupplier(val metricsRegistry: MetricRegistry) : AppMetricsSupplier {

    override fun supply(): Map<String, Number> {
        return metricsRegistry.timers.entries
                .map { entry ->
                    val name = entry.key
                    val timer = entry.value
                    if (name.contains("health") || name.contains("metrics")) {
                        return mapOf()
                    }
                    if (!StringUtils.isNumeric(name.substring(0, name.indexOf(".")))) {
                        return mapOf()
                    }

                    val namePrefix = "zmon.response.$name"
                    mapOf<String, Number>(
                            "$namePrefix.count" to timer.count,
                            "$namePrefix.meanRate" to timer.meanRate,
                            "$namePrefix.oneMinuteRate" to timer.oneMinuteRate,
                            "$namePrefix.fiveMinuteRate" to timer.fiveMinuteRate,
                            "$namePrefix.fifteenMinuteRate" to timer.fifteenMinuteRate,
                            "$namePrefix.snapshot.max" to timer.snapshot.max,
                            "$namePrefix.snapshot.mean" to timer.snapshot.mean,
                            "$namePrefix.snapshot.median" to timer.snapshot.median,
                            "$namePrefix.snapshot.min" to timer.snapshot.min,
                            "$namePrefix.snapshot.stdDev" to timer.snapshot.stdDev,
                            "$namePrefix.snapshot.75thPercentile" to timer.snapshot.get75thPercentile(),
                            "$namePrefix.snapshot.95thPercentile" to timer.snapshot.get95thPercentile(),
                            "$namePrefix.snapshot.98thPercentile" to timer.snapshot.get98thPercentile(),
                            "$namePrefix.snapshot.99thPercentile" to timer.snapshot.get99thPercentile(),
                            "$namePrefix.snapshot.999thPercentile" to timer.snapshot.get999thPercentile()
                    )
                }.flatten()
    }

}