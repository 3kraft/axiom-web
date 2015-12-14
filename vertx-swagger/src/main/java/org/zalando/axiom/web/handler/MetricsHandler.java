package org.zalando.axiom.web.handler;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.util.Strings;

import java.util.concurrent.TimeUnit;

public class MetricsHandler implements Handler<RoutingContext> {

    private MetricRegistry metricRegistry;

    private Handler<RoutingContext> subHandler;

    public MetricsHandler(MetricRegistry metricRegistry, Handler<RoutingContext> subHandler) {
        this.metricRegistry = metricRegistry;
        this.subHandler = subHandler;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String timerMetricsName = Strings.toMetricsName(request.method(), request.path());
        String histogramMetricsName = "statusCodes." + Strings.toMetricsName(request.method(), request.path());

        Histogram histogram = metricRegistry.histogram(histogramMetricsName);

        long start = System.currentTimeMillis();
        try {
            subHandler.handle(routingContext);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int statusCode = routingContext.response().getStatusCode();

            Timer allTimer = metricRegistry.timer(timerMetricsName);
            allTimer.update(duration, TimeUnit.MILLISECONDS);

            Timer statusTimer = metricRegistry.timer(Integer.toString(statusCode) + '.' + timerMetricsName);
            statusTimer.update(duration, TimeUnit.MILLISECONDS);

            histogram.update(statusCode);
        }
    }
}
