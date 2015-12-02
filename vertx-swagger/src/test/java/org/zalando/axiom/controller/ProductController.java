package org.zalando.axiom.controller;


import org.zalando.axiom.domain.PathParam;
import org.zalando.axiom.domain.Product;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProductController {

    private Map<String, Product> products = new HashMap<>();

    public Collection<Product> get(@PathParam("longitude") double longitude, @PathParam("latitude") double latitude) {
        return products.values();
    }

    public Product addProduct(Product product) {
        products.put(product.getProductId(), product);
        return product;
    }
}
