package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.AsyncFunction;
import org.zalando.axiom.web.binding.functions.AsyncStringFunction;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.domain.ProductParameter;
import org.zalando.axiom.web.domain.ProductParameterNoDefaultCtx;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

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
    public void testGetTwoQueryParamsWithoutBasePath(TestContext context) throws Exception {
        testGetTwoParams(context, "/products?latitude=1.2&longitude=1.3", "/products", "/swagger-get-two-query-params-without-base-path.json");
    }

    @Test
    public void testGetTwoQueryParams(TestContext context) throws Exception {
        testGetTwoParams(context, "/v1/products?latitude=1.2&longitude=1.3", "/products", "/swagger-get-two-query-params.json");
    }

    @Test
    public void testGetTwoRequiredQueryParamsButOneMissing(TestContext context) throws Exception {
        testGetTwoParams(context, "/v1/products?latitude=1.2", "/products", "/swagger-get-two-query-params.json", 400);
    }

    @Test
    public void testGetTwoPathParams(TestContext context) throws Exception {
        testGetTwoParams(context, "/v1/products/1.2/1.3", "/products/:latitude/:longitude", "/swagger-get-two-path-params.json");
    }

    @Test
    public void testGetQueryPathParams(TestContext context) throws Exception {
        testGetTwoParams(context, "/v1/products/1.2?longitude=1.3", "/products/:latitude", "/swagger-get-two-query-path-params.json");
    }

    private void testGetTwoParams(TestContext context, String uriWithParams, String vertxPath, String swaggerJson) {
        testGetTwoParams(context, uriWithParams, vertxPath, swaggerJson, 200);
    }

    private void testGetTwoParams(TestContext context, String uriWithParams, String vertxPath, String swaggerJson, int expectedStatusCode) {
        ProductController controller = productController(vertx, 1);

        Async async = context.async();

        TestCoordinator coordinator = getHttpClientRequest(context, uriWithParams, controller, async, expectedStatusCode);

        startHttpServer(vertx, coordinator, () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition(swaggerJson)
                    .bindTo(vertxPath)
                        .get(ProductParameter.class, (AsyncFunction<ProductParameter, Collection<Product>>)controller::getAsync)
                        .doBind()
                    .router(vertx);
            // @formatter:on
        });
    }

    private TestCoordinator getHttpClientRequest(TestContext context, String uriWithParams, ProductController controller, Async async, int expectedStatusCode) {
        return setUpGetRequest(vertx, context, async, uriWithParams, expectedStatusCode,
                response -> response.bodyHandler(body -> {
                    try {
                        if (expectedStatusCode == 200) {
                            List<Product> responseProduct = mapper.readValue(body.toString(), new TypeReference<List<Product>>() {
                            });
                            context.assertEquals(controller.getById("0"), responseProduct.get(0));
                        }
                    } catch (IOException e) {
                        context.fail(e);
                    }
                }));
    }

    @Test
    public void testGetWithParamObjectWithNoDefaultConstructor(TestContext context) throws Exception {
        ProductController controller = productController(vertx, 1);

        Async async = context.async();

        TestCoordinator coordinator = getHttpClientRequest(context, "/v1/products?latitude=1.2&longitude=1.3", controller, async, 200);

        startHttpServer(vertx, coordinator, () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition("/swagger-get-two-query-params.json")
                    .bindTo("/products")
                        .get(ProductParameterNoDefaultCtx.class, (AsyncFunction<ProductParameterNoDefaultCtx, Collection<Product>>)controller::getAsync)
                        .doBind()
                    .router(vertx);
            // @formatter:on
        });
    }

    @Test
    public void testGetById(TestContext context) throws Exception {
        int productCount = 5;
        ProductController controller = productController(vertx, productCount);

        Async async = context.async();

        TestCoordinator coordinator = getHttpClientRequestForGetById(context, controller, async);

        startHttpServer(vertx, coordinator, () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                    .bindTo("/products/:id")
                        .get(controller::getByIdAsync)
                        .doBind()
                    .router(vertx);
            // @formatter:on
        });
    }

    @Test
    public void testGetByIdShortHand(TestContext context) throws Exception {
        int productCount = 5;
        ProductController controller = productController(vertx, productCount);

        Async async = context.async();

        TestCoordinator coordinator = getHttpClientRequestForGetById(context, controller, async);

        startHttpServer(vertx, coordinator, () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                    .getById("/products/:id", controller::getByIdAsync)
                    .router(vertx);
            // @formatter:on
        });
    }

    @Test
    public void testGetAsyncError(TestContext context) throws Exception {
        Async async = context.async();

        TestCoordinator coordinator = setUpGetRequest(vertx, context, async, "/v1/products/3", 500,
                response -> context.assertEquals(500, response.statusCode()));

        Supplier<Router> routerFactory = () -> {
            // @formatter:off
            return SwaggerRouter.swaggerDefinition("/swagger-get-by-id.json")
                    .getById("/products/:id", (AsyncStringFunction<String>) (value, handler) -> {
                        new Thread(() -> {
                            handler.handle(Future.failedFuture(new RuntimeException("Exception!")));
                        }).start();
                    })
                    .router(vertx);
            // @formatter:on
        };

        startServer(coordinator, routerFactory);

    }

    private TestCoordinator getHttpClientRequestForGetById(TestContext context, ProductController controller, Async async) {
        String id = "3";
        return setUpGetRequest(vertx, context, async, "/v1/products/" + id, 200,
                response -> response.bodyHandler(body -> {
                    try {
                        Product responseProduct = mapper.readValue(body.toString(), new TypeReference<Product>() {
                        });
                        context.assertEquals(controller.getById(id), responseProduct);
                    } catch (IOException e) {
                        context.fail(e);
                    }
                }));
    }

    private void startServer(final TestCoordinator coordinator, final Supplier<Router> routerFactory) {
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start(Future<Void> startFuture) throws Exception {
                HttpServer server = vertx.createHttpServer();
                server.requestHandler(routerFactory.get()::accept).listen(8080, handler -> {
                    if (handler.succeeded()) {
                        coordinator.startRequest(server);
                        startFuture.complete();
                    } else {
                        throw new IllegalStateException("Server did not start!", handler.cause());
                    }
                });
            }
        });
    }
}
