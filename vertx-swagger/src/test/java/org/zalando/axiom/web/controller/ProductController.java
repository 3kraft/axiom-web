package org.zalando.axiom.web.controller;


import org.zalando.axiom.web.domain.PathParam;
import org.zalando.axiom.web.domain.Product;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductController {

    private final Map<String, Product> products = new HashMap<>();

    public Product getById(String id) {
        return products.get(id);
    }

    public Collection<Product> get(@PathParam("longitude") double longitude, @PathParam("latitude") double latitude) {
        return products.values();
    }

    public String create(Product product) {
        return addProduct(product).getId();
    }

    public Product addProduct(Product product) {
        product.setId(UUID.randomUUID().toString());
        products.put(product.getId(), product);
        return product;
    }
}
