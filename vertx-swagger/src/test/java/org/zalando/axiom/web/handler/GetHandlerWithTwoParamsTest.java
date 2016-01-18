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
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.AsyncFunction;
import org.zalando.axiom.web.testutil.VertxTestBuilder;
import org.zalando.axiom.web.testutil.controller.ProductController;
import org.zalando.axiom.web.testutil.domain.Product;
import org.zalando.axiom.web.testutil.domain.ProductParameterNoDefaultCtx;

import java.util.Collection;
import java.util.List;

import static org.zalando.axiom.web.testutil.Data.productController;

@RunWith(VertxUnitRunner.class)
public class GetHandlerWithTwoParamsTest {

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
    public void testGetTwoQueryParamsWithoutBasePath(TestContext context) throws Exception {
        testGetTwoParamsAndRun(context, "/products?latitude=1.2&longitude=1.3", "/products", "/swagger-get-two-query-params-without-base-path.json");
    }

    @Test
    public void testGetTwoQueryParams(TestContext context) throws Exception {
        testGetTwoParamsAndRun(context, "/v1/products?latitude=1.2&longitude=1.3", "/products", "/swagger-get-two-query-params.json");
    }

    @Test
    public void testGetTwoRequiredQueryParamsButOneMissing(TestContext context) throws Exception {
        testGetTwoParams(context, "/v1/products?latitude=1.2", "/products", "/swagger-get-two-query-params.json")
                .responseAssertion(null)
                .expectedStatusCode(400)
                .start(context, vertx);
    }

    @Test
    public void testGetTwoPathParams(TestContext context) throws Exception {
        testGetTwoParamsAndRun(context, "/v1/products/1.2/1.3", "/products/:latitude/:longitude", "/swagger-get-two-path-params.json");
    }

    @Test
    public void testGetQueryPathParams(TestContext context) throws Exception {
        testGetTwoParamsAndRun(context, "/v1/products/1.2?longitude=1.3", "/products/:latitude", "/swagger-get-two-query-path-params.json");
    }

    private void testGetTwoParamsAndRun(TestContext context, String uri, String bindingPath, String swaggerDefinition) throws Exception {
        testGetTwoParams(context, uri, bindingPath, swaggerDefinition).start(context, vertx);
    }

    private VertxTestBuilder testGetTwoParams(TestContext context, String uri, String bindingPath, String swaggerDefinition) throws Exception {
        ProductController controller = productController(vertx, 1);

        return VertxTestBuilder
                .tester()
                .routerFactory(() -> {
                    // @formatter:off
                    return SwaggerRouter.swaggerDefinition(swaggerDefinition)
                        .bindTo(bindingPath)
                            .get(ProductParameterNoDefaultCtx.class, (AsyncFunction<ProductParameterNoDefaultCtx, Collection<Product>>)controller::getAsync)
                            .doBind()
                        .router(vertx);
                    // @formatter:on
                })
                .getRequest(uri)
                .responseAssertion((testContext, response) -> response.bodyHandler(body -> {
                    try {
                        List<Product> responseProducts = mapper.readValue(body.toString(), new TypeReference<List<Product>>() {
                        });
                        context.assertEquals(controller.get(), responseProducts);
                    } catch (Exception e) {
                        context.fail(e);
                    }
                }));
    }

    @Test
    public void testGetWithParamObjectWithNoDefaultConstructor(TestContext context) throws Exception {
        testGetTwoParamsAndRun(context, "/v1/products?latitude=1.2&longitude=1.3", "/products", "/swagger-get-two-query-params.json");
    }

}
