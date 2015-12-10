package org.zalando.axiom.web;

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

    private SwaggerRouter() {
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(
                this.propertyNamingStrategy);

    }

    public static BindingBuilderFactory swaggerDefinition(String onClassPath) {
        return swaggerDefinition(onClassPath, null);
    }

    public static BindingBuilderFactory swaggerDefinition(String onClassPath, SwaggerRouterConfiguration configuration) {
        InputStream jsonStream = SwaggerRouter.class.getResourceAsStream(onClassPath);

        SwaggerRouter swaggerRouter = new SwaggerRouter();
        swaggerRouter.configure(configuration);
        swaggerRouter.swagger = swaggerRouter.load(jsonStream);

        return new BindingBuilderFactory(swaggerRouter);
    }

    private void configure(SwaggerRouterConfiguration configuration) {
        if (configuration == null) {
            return;
        }

        if (configuration.getMapper() != null) {
            this.mapper = configuration.getMapper();
        }
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    private Swagger load(InputStream jsonStream) {
        try (InputStreamReader reader = new InputStreamReader(jsonStream); Reader bufferedReader = new BufferedReader(reader)) {
            return new Swagger20Parser().read(new ObjectMapper().readTree(bufferedReader));
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }

}

