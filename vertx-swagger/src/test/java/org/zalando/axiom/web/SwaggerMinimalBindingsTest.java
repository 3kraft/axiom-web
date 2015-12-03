package org.zalando.axiom.web;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.SwaggerRouter;

import java.io.InputStream;

@RunWith(VertxUnitRunner.class)
public class SwaggerMinimalBindingsTest {

    private InputStream jsonStream;

    private Vertx vertx;

    @Before
    public void setUp() {
        jsonStream = this.getClass().getResourceAsStream("/swagger-minimal.json");
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() throws Exception {
        vertx.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testNoController() throws Exception {
        SwaggerRouter.router(vertx).setupRoutes(jsonStream);
    }

    @Test
    public void testBindingSwaggerMinimal() throws Exception {
        Router router = SwaggerRouter.router(vertx).controller(new ProductController()).setupRoutes(jsonStream);
        Assert.assertNotNull(router);
    }
}
