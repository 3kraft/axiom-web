package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.testutil.VertxTestBuilder;
import org.zalando.axiom.web.testutil.controller.ProductController;
import org.zalando.axiom.web.testutil.domain.Product;

import java.io.IOException;
import java.util.List;

import static org.zalando.axiom.web.testutil.Data.productController;

@RunWith(VertxUnitRunner.class)
public class GetHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetHandlerTest.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private Vertx vertx;
    private ProductController controller;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        controller = productController(vertx, 5);
    }

    @After
    public void tearDown() throws Exception {
        vertx.close();
    }

    @Test
    public void testGetById(TestContext context) throws Exception {
        String id = getRandomId(controller);
        setUpGetByIdTest(id, controller)
                .routerFactory(() -> {
                    // @formatter:off
                    return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                            .bindTo("/products/:id")
                                .get(controller::getByIdAsync)
                                .doBind()
                            .router(vertx);
                    // @formatter:on
                })
                .start(context, vertx);
    }

    @Test
    public void testGetByIdShortHand(TestContext context) throws Exception {
        String id = getRandomId(controller);
        setUpGetByIdTest(id, controller)
                .routerFactory(() -> {
                    // @formatter:off
                    return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                            .getById("/products/:id", controller::getByIdAsync)
                            .router(vertx);
                    // @formatter:on
                })
                .start(context, vertx);
    }

    private VertxTestBuilder setUpGetByIdTest(String id, ProductController controller) {
        return VertxTestBuilder.tester()
                .getRequest("/v1/products/" + id)
                .responseAssertion((testContext, response) -> {
                    response.bodyHandler(body -> {
                        try {
                            Product responseProduct = mapper.readValue(body.toString(), new TypeReference<Product>() {
                            });
                            testContext.assertEquals(controller.getById(id), responseProduct);
                        } catch (IOException e) {
                            testContext.fail(e);
                        }
                    });
                });
    }

    private String getRandomId(ProductController controller) {
        List<Product> products = controller.get();
        String id = Long.toString(Math.round(Math.random() * (products.size() - 1)));
        LOGGER.info(String.format("Chosen product id is [%s].", id));
        return id;
    }
}
