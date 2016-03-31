package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.unary.AsyncStringFunction;
import org.zalando.axiom.web.testutil.VertxTestBuilder;

@RunWith(VertxUnitRunner.class)
public class GetHandlerAsyncErrorTest {

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
    public void testGetAsyncError(TestContext context) throws Exception {
        VertxTestBuilder.tester()
                .expectedStatusCode(500)
                .routerFactory(() -> {
                    // @formatter:off
                    return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                            .getById("/products/:id", (AsyncStringFunction<String>) (value, handler) -> {
                                new Thread(() -> {
                                    handler.handle(Future.failedFuture(new RuntimeException("Exception!")));
                                }).start();
                            })
                            .router(vertx);
                    // @formatter:on
                })
                .getRequest("/v1/products/3")
                .start(context, vertx);
    }
}
