package org.zalando.axiom.web.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.BindingBuilderFactory;

public class SwaggerRouterConfiguration {

    private ObjectMapper mapper;

    private boolean collectMetrics;

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
        this.collectMetrics = true;
        return this;
    }

    public boolean isCollectMetrics() {
        return collectMetrics;
    }
}
