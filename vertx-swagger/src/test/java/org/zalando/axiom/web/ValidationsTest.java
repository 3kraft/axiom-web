package org.zalando.axiom.web;

import io.vertx.core.Vertx;
import org.junit.Before;
import org.junit.Test;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;

public class ValidationsTest {

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @Test(expected = IllegalStateException.class)
    public void testPathAddedTwice() throws Exception {
        ProductController controller = new ProductController(vertx);
        SwaggerRouter.swaggerDefinition("/swagger-post.json")
                        .bindTo("/products")
                            .post(Product.class, controller::create)
                            .doBind()
                        .bindTo("/products")
                            .post(Product.class, controller::create)
                            .doBind();
    }
}
