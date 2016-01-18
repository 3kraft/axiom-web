package org.zalando.axiom.web;

import io.vertx.core.Vertx;
import org.junit.Before;
import org.junit.Test;
import org.zalando.axiom.web.binding.functions.AsyncFunction;
import org.zalando.axiom.web.testutil.controller.ProductController;
import org.zalando.axiom.web.testutil.domain.Product;

public class ValidationsTest {

    private Vertx vertx;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @Test(expected = IllegalStateException.class)
    public void testPathAddedTwice() throws Exception {
        ProductController controller = new ProductController(vertx);
        // @formatter:off
        SwaggerRouter.swaggerDefinition("/swagger-post.json")
                        .bindTo("/products")
                            .post(Product.class, (AsyncFunction<Product, String>) controller::create)
                            .doBind()
                        .bindTo("/products")
                            .post(Product.class, (AsyncFunction<Product, String>) controller::create)
                            .doBind();
        // @formatter:on
    }
}
