package org.zalando.axiom.web.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SwaggerRouterConfiguration {

    private ObjectMapper mapper;

    public SwaggerRouterConfiguration mapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
