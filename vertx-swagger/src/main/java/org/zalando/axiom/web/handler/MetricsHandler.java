package org.zalando.axiom.web.handler;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.zalando.axiom.web.util.Strings;

import java.util.concurrent.TimeUnit;

public class MetricsHandler implements Handler<RoutingContext> {

    private final MetricRegistry metricRegistry;

    private final Handler<RoutingContext> subHandler;

    private final String path;

    public MetricsHandler(MetricRegistry metricRegistry, Handler<RoutingContext> subHandler, String path) {
        this.metricRegistry = metricRegistry;
        this.subHandler = subHandler;
        this.path = path;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        String timerMetricsName = Strings.toMetricsName(request.method(), path);
        String counterMetricsName = "statusCodes." + Strings.toMetricsName(request.method(), path);


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

            Counter counter = metricRegistry.counter(counterMetricsName + '.' + statusCode);
            counter.inc();
        }
    }
}
