package org.zalando.axiom.web.krueger


import com.codahale.metrics.MetricRegistry
import io.vertx.core.AsyncResultHandler
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import org.apache.logging.log4j.LogManager
import org.zalando.axiom.web.SwaggerRouter
import org.zalando.axiom.web.krueger.metrics.AppMetricsSupplier
import org.zalando.axiom.web.krueger.metrics.GarbageCollectorMetricsSupplier
import org.zalando.axiom.web.krueger.metrics.JvmMetricsSupplier
import org.zalando.axiom.web.krueger.metrics.ZmonMetricsSupplier
import java.util.*

object KruegerRouter {

    val log = LogManager.getLogger(javaClass)
    val swaggerJson = "operations-swagger.json"

    fun create(vertx: Vertx, metricsRegistry: MetricRegistry = MetricRegistry(),
               metricsSuppliers: List<AppMetricsSupplier> = listOf(ZmonMetricsSupplier(metricsRegistry), JvmMetricsSupplier(), GarbageCollectorMetricsSupplier())): Router {

        val factory = SwaggerRouter.configure()
                .collectMetricsTo(metricsRegistry)
                .mapper(Json.mapper)
                .swaggerDefinition("/$swaggerJson")

        val appMetricsService = AppMetricsService(vertx, metricsSuppliers)
        val vertxMetricsService = VertxMetricsService(vertx, metricsRegistry)

        val router =
                factory.bindTo("/metrics")
                        .get {  handler: AsyncResultHandler<Map<String, Number>> -> appMetricsService.getMetrics(handler) }
                        .doBind()
                        .bindTo("/metrics/statusCodes")
                        .get { handler: AsyncResultHandler<Map<String, Long>> -> vertxMetricsService.getStatusCodesMetrics(handler) }
                        .doBind()
                        .bindTo("/metrics/vertx")
                        .get { handler: AsyncResultHandler<List<JsonObject>> -> vertxMetricsService.getVertxMetrics(handler) }
                        .doBind()
                        .bindTo("/metrics/eventBus")
                        .get { handler: AsyncResultHandler<Map<String, Number>> -> vertxMetricsService.getEventBusMetrics(handler) }
                        .doBind()
                        .bindTo("/env")
                        .get { handler: AsyncResultHandler<Map<String, String>> -> handler.handle(Future.succeededFuture(System.getenv())) }
                        .doBind()
                        .bindTo("/properties")
                        .get { handler: AsyncResultHandler<Properties> -> handler.handle(Future.succeededFuture(System.getProperties())) }
                        .doBind()
                        .bindTo("/health")
                        .get { handler: AsyncResultHandler<String> -> handler.handle(Future.succeededFuture("ok")) }
                        .doBind()
                        .router(vertx)

        router.route("/$swaggerJson").handler { ctx ->
            ctx.response().sendFile(swaggerJson)
        }

        router.exceptionHandler({ e: kotlin.Throwable -> log.error("Error while handling request", e) })

        return router;
    }

}
