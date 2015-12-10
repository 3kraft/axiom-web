package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.domain.ProductParameter;

import java.io.IOException;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class GetHandlerTest {

    private Vertx vertx;

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    @After
    public void tearDown() throws Exception {
        vertx.close();
    }

    @Test
    public void testGet(TestContext context) throws Exception {
        Product product = new Product();
        product.setCapacity("capacity");
        product.setDescription("description");
        product.setDisplayName("product name");

        ProductController controller = new ProductController();
        controller.addProduct(product);

        Async async = context.async();

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                // @formatter:off
                Router router = SwaggerRouter.swaggerDefinition("/swagger-minimal.json")
                        .bindTo("/products")
                            .get(ProductParameter.class, controller::get)
                            .doBind()
                        .router(vertx);
                // @formatter:on

                vertx.createHttpServer().requestHandler(router::accept).listen(8080);
            }
        });

        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.get(8080, "127.0.0.1", "/v1/products?latitude=1.2&longitude=1.3");
        request.handler(response -> {
            if (response.statusCode() != 200) {
                context.fail(String.format("Status code is [%d]", response.statusCode()));
                async.complete();
            }
            response.bodyHandler(body -> {
                try {
                    List<Product> responseProduct = mapper.readValue(body.toString(), new TypeReference<List<Product>>() {
                    });
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

    @Test
    public void testGetById(TestContext context) throws Exception {
        Product product = new Product();
        product.setCapacity("capacity");
        product.setDescription("description");
        product.setDisplayName("product name");

        ProductController controller = new ProductController();
        String id = controller.addProduct(product).getId();

        Async async = context.async();

        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                // @formatter:off
                Router router = SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                        .bindTo("/products/:id")
                            .get(controller::getById)
                            .doBind()
                        .router(vertx);
                // @formatter:on

                vertx.createHttpServer().requestHandler(router::accept).listen(8080);
            }
        });

        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.get(8080, "127.0.0.1", "/v1/products/" + id);
        request.handler(response -> {
            response.bodyHandler(body -> {
                try {
                    Product responseProduct = mapper.readValue(body.toString(), new TypeReference<Product>() {
                    });
                    context.assertEquals(product, responseProduct);
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

        Thread.sleep(300);
        request.end();
        Thread.sleep(200);
    }

}
