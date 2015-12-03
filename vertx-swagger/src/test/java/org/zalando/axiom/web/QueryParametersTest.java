package org.zalando.axiom.web;

import org.junit.Test;
import org.zalando.axiom.web.controller.TypeController;

import java.io.InputStream;
import java.util.Arrays;

public class QueryParametersTest {

    private SwaggerRouter loadRouter(final String jsonPath, Object ...controllers) {
        InputStream jsonStream = this.getClass().getResourceAsStream(jsonPath);
        SwaggerRouter router = SwaggerRouter.router(null);
        Arrays.stream(controllers).forEach(controller -> router.controller(controller));
        router.setupRoutes(jsonStream);
        return router;
    }

    @Test
    public void testParameterTypes() throws Exception {
        loadRouter("/swagger-parameter-test.json", new TypeController());
    }
}
