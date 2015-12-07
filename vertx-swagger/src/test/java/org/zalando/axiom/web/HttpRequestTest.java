package org.zalando.axiom.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.verticle.WebVerticle;

import java.io.IOException;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class HttpRequestTest {

    private Vertx vertx;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() throws Exception {
        vertx.close();
    }

    @Test
    public void testLoading(TestContext context) throws Exception {
        Product product = new Product();
        product.setCapacity("capacity");
        product.setDescription("description");
        product.setDisplayName("product name");

        ProductController controller = new ProductController();
        controller.addProduct(product);

        Async async = context.async();

        vertx.deployVerticle(new WebVerticle("/swagger-minimal.json", controller));

        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.get(8080, "127.0.0.1", "/v1/products?latitude=1.2&longitude=1.3");
        request.handler(response -> {
            response.bodyHandler(body -> {
                try {
                    List<Product> responseProduct = mapper.readValue(body.toString(), new TypeReference<List<Product>>() {});
                    context.assertEquals(product, responseProduct.get(0));
                } catch (IOException e) {
                    context.fail(e);
                }
            });
            async.complete();
        });
        request.exceptionHandler(exception -> {
            context.fail(exception.getLocalizedMessage());
            async.complete();
        });

        Thread.sleep(200);
        request.end();
        Thread.sleep(200);

    }

    // TODO no method in controller matching operation id found
    // TODO list query parameters
    // TODO throw exception if one query parameter is missing
    // TODO support wrapper types
    // TODO support date, datetime types
    // TODO check required parameters
    // TODO type validation: maximum, minimum, pattern, etc.
    // TODO expose property naming strategy as option PropertyNamingStrategy
}
