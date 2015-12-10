package org.zalando.axiom.web.controller;


import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.domain.ProductParameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductController {

    private final Map<String, Product> products = new HashMap<>();

    public Product getById(String id) {
        return products.get(id);
    }

    public Collection<Product> get(ProductParameter parameter) {
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
