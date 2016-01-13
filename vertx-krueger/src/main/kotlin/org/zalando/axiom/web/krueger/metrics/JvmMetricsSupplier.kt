package org.zalando.axiom.web.krueger.metrics

import java.lang.management.ManagementFactory


public class JvmMetricsSupplier(val appStart: Long= System.currentTimeMillis()) : AppMetricsSupplier {

    /*
    Code and naming borrowed from: org.springframework.boot.actuate.endpoint.SystemPublicMetrics

    https://github.com/spring-projects/spring-boot/blob/master/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/endpoint/SystemPublicMetrics.java

     */
    override fun supply(): Map<String, Number> {
        val values = sortedMapOf<String, Number>()
        val runtime = Runtime.getRuntime()
        val classLoadingMxBean = ManagementFactory.getClassLoadingMXBean()
        val memoryUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage;
        val threadMxBean = ManagementFactory.getThreadMXBean();

        values["classes"] = classLoadingMxBean.loadedClassCount
        values["classes.loaded"] = classLoadingMxBean.totalLoadedClassCount
        values["classes.unloaded"] = classLoadingMxBean.unloadedClassCount
        values["heap"] = memoryUsage.max
        values["heap.committed"] = memoryUsage.committed
        values["heap.init"] = memoryUsage.init
        values["heap.used"] = memoryUsage.used
        values["nonheap.committed"] = memoryUsage.committed;
        values["nonheap.init"] = memoryUsage.init;
        values["nonheap.used"] = memoryUsage.used;
        values["nonheap"] = memoryUsage.max;
        values["mem"] = runtime.totalMemory() + getTotalNonHeapMemoryIfPossible()
        values["mem.free"] = runtime.freeMemory()
        values["processors"] = runtime.availableProcessors()
        values["threads"] = threadMxBean.threadCount
        values["threads.daemon"] = threadMxBean.daemonThreadCount
        values["threads.peak"] = threadMxBean.peakThreadCount
        values["threads.totalStarted"] = threadMxBean.totalStartedThreadCount
        values["uptime"] = ManagementFactory.getRuntimeMXBean().uptime
        values["instance.uptime"] = System.currentTimeMillis() - appStart
        values["systemload.average"] = ManagementFactory.getOperatingSystemMXBean().systemLoadAverage

        return values
    }

    private fun getTotalNonHeapMemoryIfPossible(): Long {
        try {
            return ManagementFactory.getMemoryMXBean().nonHeapMemoryUsage.used;
        } catch (ex: Throwable) {
            return 0;
        }
    }

}