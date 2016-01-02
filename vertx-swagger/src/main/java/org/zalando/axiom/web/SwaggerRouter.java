package org.zalando.axiom.web;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import org.zalando.axiom.web.binding.BindingBuilderFactory;
import org.zalando.axiom.web.domain.SwaggerRouterConfiguration;
import org.zalando.axiom.web.exceptions.LoadException;

import java.io.*;

public final class SwaggerRouter {

    private Swagger swagger;

    private ObjectMapper mapper;

    private PropertyNamingStrategy propertyNamingStrategy = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;

    private MetricRegistry metricsRegistry;

    private SwaggerRouter() {
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(
                this.propertyNamingStrategy);

    }

    public static BindingBuilderFactory swaggerDefinition(String onClassPath) {
        return swaggerDefinition(onClassPath, null);
    }

    public static BindingBuilderFactory swaggerDefinition(String onClassPath, SwaggerRouterConfiguration configuration) {
        try (InputStream jsonStream = SwaggerRouter.class.getResourceAsStream(onClassPath)) {

            SwaggerRouter swaggerRouter = new SwaggerRouter();
            swaggerRouter.configure(configuration);
            swaggerRouter.swagger = swaggerRouter.load(jsonStream);

            return new BindingBuilderFactory(swaggerRouter);
        } catch (IOException e) {
            throw new LoadException("Could not open swagger definition!", e);
        }
    }

    public static SwaggerRouterConfiguration configure() {
        return new SwaggerRouterConfiguration();
    }

    public SwaggerRouter configure(SwaggerRouterConfiguration configuration) {
        if (configuration == null) {
            return this;
        }

        if (configuration.getMapper() != null) {
            this.mapper = configuration.getMapper();
        }

        if (configuration.isCollectMetrics()) {
            this.metricsRegistry = configuration.getMetricRegistry();
        }

        return this;
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public MetricRegistry getMetricsRegistry() {
        return metricsRegistry;
    }

    public boolean isMetricsEnabled() {
        return getMetricsRegistry() != null;
    }

    private Swagger load(InputStream jsonStream) {
        try (InputStreamReader reader = new InputStreamReader(jsonStream, "UTF-8"); Reader bufferedReader = new BufferedReader(reader)) {
            return new Swagger20Parser().read(new ObjectMapper().readTree(bufferedReader));
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }

}

