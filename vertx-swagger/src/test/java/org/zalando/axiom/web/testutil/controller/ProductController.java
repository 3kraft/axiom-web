package org.zalando.axiom.web.testutil.controller;


import io.vertx.core.*;
import org.zalando.axiom.web.testutil.domain.Product;
import org.zalando.axiom.web.testutil.domain.ProductParameter;
import org.zalando.axiom.web.testutil.domain.ProductParameterNoDefaultCtx;
import org.zalando.axiom.web.util.Preconditions;

import java.util.*;

public class ProductController {

    private final Map<String, Product> products = new HashMap<>();

    private Vertx vertx;

    public ProductController(Vertx vertx) {
        this.vertx = vertx;
    }

    public List<Product> get() {
        return new ArrayList<>(products.values());
    }
    public Product getById(String id) {
        return products.get(id);
    }

    public String create(Product product) {
        return addProduct(product).getId();
    }

    public void create(Product product, AsyncResultHandler<String> handler) {
        vertx.executeBlocking(event -> {
                    event.complete(this.create(product));
                },
                event -> {
                    if (event.succeeded()) {
                        handler.handle(Future.succeededFuture((String) event.result()));
                    } else {
                        handler.handle(Future.failedFuture(event.cause().getMessage()));
                    }
                });
    }

    public Product addProduct(Product product) {
        if (product.getId() == null) {
            product.setId(UUID.randomUUID().toString());
        }
        products.put(product.getId(), product);
        return product;
    }

    public void addProduct(Product product, AsyncResultHandler<Product> handler) {
        vertx.executeBlocking(event -> {
                    event.complete(this.addProduct(product));
                },
                event -> {
                    if (event.succeeded()) {
                        handler.handle(Future.succeededFuture((Product) event.result()));
                    } else {
                        handler.handle(Future.failedFuture(event.cause().getMessage()));
                    }
                });
    }


    public void deleteProduct(String id) {
        products.remove(id);
    }

    public void deleteProductAsync(String id, AsyncResultHandler<Void> handler) {
        vertx.executeBlocking(event -> {
                    handler.handle(Future.succeededFuture());
                    event.complete();
                }, event -> {
                    if (event.failed()) {
                        handler.handle(Future.failedFuture(event.cause()));
                    }
                }
        );
    }

    public void getAsync(ProductParameter parameter, AsyncResultHandler<Collection<Product>> handler) {
        vertx.executeBlocking(event -> {
            handler.handle(Future.succeededFuture(products.values()));
            event.complete();
        }, handleAsyncResult(handler));
    }

    private Handler<AsyncResult<Object>> handleAsyncResult(AsyncResultHandler<Collection<Product>> handler) {
        return event -> {
            if (event.failed()) {
                handler.handle(Future.failedFuture(event.cause()));
            }
        };
    }

    public void getAsync(ProductParameterNoDefaultCtx parameter, AsyncResultHandler<Collection<Product>> handler) {
        vertx.executeBlocking(event -> {
            handler.handle(Future.succeededFuture(products.values()));
            event.complete();
        }, handleAsyncResult(handler));
    }

    public void getByIdAsync(String id, AsyncResultHandler<Product> handler) {
        vertx.<Product>executeBlocking(event -> event.complete(getById(id)), event -> {
            if (event.succeeded()) {
                handler.handle(Future.succeededFuture(event.result()));
            }
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
