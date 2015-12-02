package org.zalando.axiom;

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
import org.zalando.axiom.controller.ProductController;
import org.zalando.axiom.domain.Product;
import org.zalando.axiom.verticle.WebVerticle;

@RunWith(VertxUnitRunner.class)
public class HttpRequestTest {

    private Vertx vertx;

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
                String stringValue = String.valueOf(body.getByteBuf());
            });
            context.assertEquals(product, request);
            async.complete();
        });
        request.exceptionHandler(exception -> {
            context.fail(exception.getLocalizedMessage());
            async.complete();
        });
        request.end();

    }

    // TODO no controller with operation id name found
    // TODO no method in controller matchin operation id found
    // TODO list query parameters
    // TODO test getter with one parameter without path param annotation
    // TODO throw exception if one query parameter is missing
}
