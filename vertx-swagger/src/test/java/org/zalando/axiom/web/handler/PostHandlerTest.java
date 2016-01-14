package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.binding.functions.AsyncConsumer;
import org.zalando.axiom.web.binding.functions.AsyncFunction;
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;
import org.zalando.axiom.web.util.VertxUtils;

import java.util.function.Supplier;

import static org.zalando.axiom.web.util.Data.product;

@RunWith(VertxUnitRunner.class)
public class PostHandlerTest {

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
    public void testPostHandler(TestContext context) throws Exception {
        ProductController controller = new ProductController(vertx);
        testPost(context,
                // @formatter:off
                () -> SwaggerRouter.swaggerDefinition("/swagger-post.json")
                    .bindTo("/products")
                        .post(Product.class, (AsyncFunction<Product, String>) controller::create)
                        .doBind()
                    .router(vertx));
                // @formatter:on);
    }

    @Test
    public void testPostHandlerWithoutLocation(TestContext context) throws Exception {
        ProductController controller = new ProductController(vertx);
        testPost(context, false,
                // @formatter:off
                () -> SwaggerRouter.swaggerDefinition("/swagger-post.json")
                    .bindTo("/products")
                        .post(Product.class, (AsyncFunction<Product, String>) controller::create)
                        .doBind()
                    .router(vertx));
                // @formatter:on);
    }

    @Test
    public void testPostHandlerIdInResultObject(TestContext context) throws Exception {
        ProductController controller = new ProductController(vertx);
        testPost(context,
                // @formatter:off
                () -> SwaggerRouter.swaggerDefinition("/swagger-post-id-from-object.json")
                    .bindTo("/products")
                        .post(Product.class, (AsyncFunction<Product, Product>) controller::addProduct)
                        .doBind()
                    .router(vertx));
                // @formatter:on);
    }

    @Test
    public void testPostAsyncHandlerIdInResultObject(TestContext context) throws Exception {
        ProductController controller = new ProductController(vertx);
        testPost(context,
                // @formatter:off
                () -> SwaggerRouter.swaggerDefinition("/swagger-post-id-from-object.json")
                    .bindTo("/products")
                        .post(Product.class, controller::addProductAsync)
                        .doBind()
                    .router(vertx));
                // @formatter:on);
    }

    private void testPost(TestContext context, Supplier<Router> routerFactory) throws Exception {
        testPost(context, true, routerFactory);
    }

    private void testPost(TestContext context, boolean withLocation, Supplier<Router> routerFactory) throws Exception {
        Product product = product(0);
        Async async = context.async();
        TestCoordinator coordinator = getHttpClientRequest(context, async, product);
        VertxUtils.startHttpServer(vertx, coordinator, mapper.writeValueAsString(product), null, routerFactory);
    }

    private TestCoordinator getHttpClientRequest(TestContext context, Async async, Product product) {
        return getHttpClientRequest(context, async, product, true);
    }

    private TestCoordinator getHttpClientRequest(TestContext context, Async async, Product product, boolean withLocation) {
        final String uri = "/v1/products";
        return VertxUtils.setUpPostRequest(vertx, context, async, uri, 201, response -> {
            String location = response.headers().get(HttpHeaders.LOCATION);
            String expectedLocation = uri + "/" + product.getId();
            if (!location.equals(expectedLocation)) {
                context.fail(String.format("Location [%s] does not match uri [%s].", location, expectedLocation));
            }
        });
    }
}
