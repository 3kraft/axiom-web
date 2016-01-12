package org.zalando.axiom.web.controller;


import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.domain.ProductParameter;
import org.zalando.axiom.web.domain.ProductParameterNoDefaultCtx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public void getAsync(String id, AsyncResultHandler<Product> handler) {
        vertx.executeBlocking(event -> {
            Product product = getById(id);
            handler.handle(Future.succeededFuture(product));
            event.complete();
        }, event -> {
            if (event.failed()) {
                handler.handle(Future.failedFuture(event.cause()));
            }
        });
    }

    public void addProductAsync(Product product, AsyncResultHandler<Product> handler) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        products.put(product.getId(), product);
        vertx.executeBlocking(event -> {
            handler.handle(Future.succeededFuture(addProduct(product)));
            event.complete();
        }, event -> {
            if (event.failed()) {
                handler.handle(Future.failedFuture(event.cause()));
            }
        });
    }


}
