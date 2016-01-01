package org.zalando.axiom.web;

import org.junit.Test;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;

public class ValidationsTest {

    @Test(expected = IllegalStateException.class)
    public void testPathAddedTwice() throws Exception {
        ProductController controller = new ProductController();
        SwaggerRouter.swaggerDefinition("/swagger-post.json")
                        .bindTo("/products")
                            .post(Product.class, controller::create)
                            .doBind()
                        .bindTo("/products")
                            .post(Product.class, controller::create)
                            .doBind();
    }
}
