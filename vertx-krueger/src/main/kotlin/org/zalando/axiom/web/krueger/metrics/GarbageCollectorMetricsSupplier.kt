package org.zalando.axiom.web.krueger.metrics

import org.apache.commons.lang.StringUtils
import org.zalando.axiom.web.krueger.flatten
import java.lang.management.ManagementFactory


public class GarbageCollectorMetricsSupplier() : AppMetricsSupplier {

    override fun supply(): Map<String, Number> {
        val garbageCollectorMxBeans = ManagementFactory.getGarbageCollectorMXBeans();
        return garbageCollectorMxBeans.map { garbageCollectorMXBean ->
            val name = beautifyGcName(garbageCollectorMXBean.name);
            mapOf<String, Number>(
                    "gc.$name.count" to garbageCollectorMXBean.collectionCount,
                    "gc.$name.time" to garbageCollectorMXBean.collectionTime
            )
        }.flatten()
    }


    /**
     * Turn GC names like 'PS Scavenge' or 'PS MarkSweep' into something that is more
     * metrics friendly.
     * @param name the source name
     * @return a metric friendly name
     */
    private fun beautifyGcName(name: String) = StringUtils.replace(name, " ", "_").toLowerCase();

}