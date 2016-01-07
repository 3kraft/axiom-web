package org.zalando.axiom.web.handler;

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
import org.zalando.axiom.web.util.Data;

import static org.zalando.axiom.web.util.VertxUtils.setUpDeleteRequest;
import static org.zalando.axiom.web.util.VertxUtils.startHttpServer;

@RunWith(VertxUnitRunner.class)
public class DeleteHandlerTest {

    private Vertx vertx;

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    }

    @After
    public void tearDown() throws Exception {
        vertx.close();
    }

    @Test
    public void testDelete(TestContext context) throws Exception {
        Async async = context.async();

        ProductController controller = Data.productController(vertx, 5);
        HttpClientRequest request = setUpDeleteRequest(vertx, context, async, "/v1/products/" + 3, 204);

        startHttpServer(vertx, request, () -> {
                // @formatter:off
                return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                            .bindTo("/products/:id")
                                .delete(controller::deleteProduct)
                            .doBind()
                            .router(vertx);
                // @formatter:on
        });
    }
}
