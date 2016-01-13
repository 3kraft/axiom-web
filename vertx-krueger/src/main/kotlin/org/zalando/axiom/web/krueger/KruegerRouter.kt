package org.zalando.axiom.web.krueger


import com.codahale.metrics.MetricRegistry
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import org.apache.logging.log4j.LogManager
import org.zalando.axiom.web.SwaggerRouter
import org.zalando.axiom.web.krueger.metrics.AppMetricsSupplier
import org.zalando.axiom.web.krueger.metrics.GarbageCollectorMetricsSupplier
import org.zalando.axiom.web.krueger.metrics.JvmMetricsSupplier
import org.zalando.axiom.web.krueger.metrics.ZmonMetricsSupplier

object KruegerRouter {

    val log = LogManager.getLogger(javaClass)
    val swaggerJson = "operations-swagger.json"

    fun create(vertx: Vertx, metricsRegistry: MetricRegistry = MetricRegistry(),
               metricsSuppliers: List<AppMetricsSupplier> = listOf(ZmonMetricsSupplier(metricsRegistry), JvmMetricsSupplier(), GarbageCollectorMetricsSupplier())): Router {

        val factory = SwaggerRouter.configure()
                .collectMetricsTo(metricsRegistry)
                .mapper(Json.mapper)
                .swaggerDefinition("/$swaggerJson")

        val appMetricsService = AppMetricsService(metricsSuppliers)
        val vertxMetricsService = VertxMetricsService(vertx, metricsRegistry)

        val router =
                factory.bindTo("/metrics")
                        .get { -> appMetricsService.getMetrics() }
                        .doBind()
                        .bindTo("/metrics/statusCodes")
                        .get { -> vertxMetricsService.getStatusCodesMetrics() }
                        .doBind()
                        .bindTo("/metrics/vertx")
                        .get { -> vertxMetricsService.getVertxMetrics() }
                        .doBind()
                        .bindTo("/metrics/eventBus")
                        .get { -> vertxMetricsService.getEventBusMetrics() }
                        .doBind()
                        .bindTo("/env")
                        .get { -> System.getenv() }
                        .doBind()
                        .bindTo("/properties")
                        .get { -> System.getProperties() }
                        .doBind()
                        .bindTo("/health")
                        .get { -> "ok" }
                        .doBind()
                        .router(vertx)

        router.route("/$swaggerJson").handler { ctx ->
            ctx.response().sendFile(swaggerJson)
        }

        router.exceptionHandler({ e: kotlin.Throwable -> log.error("Error while handling request", e) })

        return router;
    }

}

