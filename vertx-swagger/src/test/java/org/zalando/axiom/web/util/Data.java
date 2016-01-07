package org.zalando.axiom.web.util;

import io.vertx.core.Vertx;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;

public final class Data {

    private Data() {
    }

    public static ProductController productController(Vertx vertx, int productCount) {
        ProductController productController = new ProductController(vertx);

        for (int i = 0; i < productCount; ++i) {
            Product product = product(i);
            productController.addProduct(product);
        }
        return productController;
    }

    public static Product product(int i) {
        Product product = new Product();
        product.setId(Integer.toString(i));
        product.setDisplayName("product " + 1);
        product.setCapacity("capacity " + 1);
        product.setDescription("description " + 1);
        return product;
    }
}
