package org.zalando.axiom.web.controller;


import io.vertx.core.Vertx;
import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.domain.ProductParameter;
import org.zalando.axiom.web.domain.ProductParameterNoDefaultCtx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ProductController {

    private Vertx vertx;

    public ProductController(Vertx vertx) {
        this.vertx = vertx;
    }

    private final Map<String, Product> products = new HashMap<>();

    public Product getById(String id) {
        return products.get(id);
    }

    public Collection<Product> get(ProductParameter parameter) {
        return products.values();
    }

    public Collection<Product> get(ProductParameterNoDefaultCtx parameter) {
        return products.values();
    }

    public String create(Product product) {
        return addProduct(product).getId();
    }

    public Product addProduct(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        products.put(product.getId(), product);
        return product;
    }

    public void deleteProduct(String id) {
        products.remove(id);
    }

    public void getAsync(String id, Consumer<Product> callback) {
        vertx.executeBlocking(event -> {
            Product product = getById(id);
            callback.accept(product);
            event.complete();
        }, event -> {});

    }

    public void addProductAsync(Product product, Consumer<Product> callback) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        products.put(product.getId(), product);
        vertx.executeBlocking(event -> {
            callback.accept(addProduct(product));
            event.complete();
        }, event -> {});
    }


}
