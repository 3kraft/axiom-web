package org.zalando.axiom.web.handler;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.testutil.Data;
import org.zalando.axiom.web.testutil.VertxTestBuilder;
import org.zalando.axiom.web.testutil.controller.ProductController;

@RunWith(VertxUnitRunner.class)
public class DeleteHandlerTest {

    private static final int PRODUCT_COUNT = 5;
    private Vertx vertx;
    private ProductController controller;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        this.controller = Data.productController(vertx, PRODUCT_COUNT);
    }

    @After
    public void tearDown() throws Exception {
        vertx.close();
    }

    @Test
    public void testDelete(TestContext context) throws Exception {
        setUpDeleteTest(context, "/swagger-get-by-id.json")
                .start(context, vertx);
    }

    @Test
    public void testDeleteCheckStatusCodesMissing(TestContext context) throws Exception {
        setUpDeleteTest(context, "/swagger-delete-response-status-code-missing.json")
                .expectedExceptionInRouterBinding(IllegalStateException.class)
                .start(context, vertx);
    }

    private VertxTestBuilder setUpDeleteTest(TestContext context, String swaggerDefinition) {
        return VertxTestBuilder.tester()
                .deleteRequest("/v1/products/3")
                .expectedStatusCode(204)
                .routerFactory(() -> {
                    // @formatter:off
                    return SwaggerRouter.swaggerDefinition(swaggerDefinition)
                                .bindTo("/products/:id")
                                    .delete(controller::deleteProductAsync)
                                .doBind()
                                .router(vertx);
                    // @formatter:on
                })
                .responseAssertion((testContext, response) -> context.assertEquals(PRODUCT_COUNT - 1, controller.get().size()));
    }
}
