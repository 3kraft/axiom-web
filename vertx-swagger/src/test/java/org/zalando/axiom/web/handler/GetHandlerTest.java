package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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

import static org.zalando.axiom.web.util.Data.productController;
import static org.zalando.axiom.web.util.VertxUtils.setUpGetRequest;
import static org.zalando.axiom.web.util.VertxUtils.startHttpServer;

@RunWith(VertxUnitRunner.class)
public class GetHandlerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private Vertx vertx;

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
        ProductController controller = productController(1);

        Async async = context.async();

        HttpClientRequest request = setUpGetRequest(vertx, context, async, "/v1/products?latitude=1.2&longitude=1.3", 200,
                response -> response.bodyHandler(body -> {
                    try {
                        List<Product> responseProduct = mapper.readValue(body.toString(), new TypeReference<List<Product>>() {
                        });
                        context.assertEquals(controller.getById("0"), responseProduct.get(0));
                    } catch (IOException e) {
                        context.fail(e);
                    }
                }));

        startHttpServer(vertx, request, () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition("/swagger-minimal.json")
                    .bindTo("/products")
                        .get(ProductParameter.class, controller::get)
                        .doBind()
                    .router(vertx);
            // @formatter:on
        });

    }

    @Test
    public void testGetById(TestContext context) throws Exception {
        int productCount = 5;
        ProductController controller = productController(productCount);

        Async async = context.async();

        String id = "3";
        HttpClientRequest request = setUpGetRequest(vertx, context, async, "/v1/products/" + id, 200,
                response -> response.bodyHandler(body -> {
                    try {
                        Product responseProduct = mapper.readValue(body.toString(), new TypeReference<Product>() {
                        });
                        context.assertEquals(controller.getById(id), responseProduct);
                    } catch (IOException e) {
                        context.fail(e);
                    }
                }));

        startHttpServer(vertx, request, () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                    .bindTo("/products/:id")
                        .get(controller::getById)
                        .doBind()
                    .router(vertx);
            // @formatter:on
        });
    }
}
