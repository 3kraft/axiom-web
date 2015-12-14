package org.zalando.axiom.web.domain;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.BindingBuilderFactory;

public class SwaggerRouterConfiguration {

    private ObjectMapper mapper;

    private MetricRegistry metricRegistry;

    public BindingBuilderFactory swaggerDefinition(String onClassPath) {
        return SwaggerRouter.swaggerDefinition(onClassPath, this);
    }

    public SwaggerRouterConfiguration mapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public SwaggerRouterConfiguration collectMetrics() {
        this.metricRegistry = new MetricRegistry();
        return this;
    }

    public SwaggerRouterConfiguration collectMetricsTo(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        return this;
    }

    public boolean isCollectMetrics() {
        return metricRegistry != null;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
