package org.zalando.axiom.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import org.zalando.axiom.web.binding.BindingBuilderFactory;
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
        InputStream jsonStream = SwaggerRouter.class.getResourceAsStream(onClassPath);

        SwaggerRouter swaggerRouter = new SwaggerRouter();
        swaggerRouter.swagger = swaggerRouter.load(jsonStream);

        return new BindingBuilderFactory(swaggerRouter);
    }

    public SwaggerRouter propertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
        return this;
    }

    public SwaggerRouter mapper(ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
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

