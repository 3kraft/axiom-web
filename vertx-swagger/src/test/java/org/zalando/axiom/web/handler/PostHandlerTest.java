package org.zalando.axiom.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
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
import org.zalando.axiom.web.controller.ProductController;
import org.zalando.axiom.web.domain.Product;

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
        Product product = new Product();
        product.setCapacity("capacity");
        product.setDescription("description");
        product.setDisplayName("product name");

        Async async = context.async();

        ProductController controller = new ProductController();
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                Router router = SwaggerRouter.swaggerDefinition("/swagger-post.json")
                        .bindTo("/v1/products")
                        .post(Product.class, controller::create)
                        .doBind()
                        .router(vertx);
                vertx.createHttpServer().requestHandler(router::accept).listen(8080);
            }
        });

        String uri = "/v1/products";

        HttpClient client = vertx.createHttpClient();
        HttpClientRequest request = client.post(8080, "127.0.0.1", uri, response -> {
            String location = response.headers().get(HttpHeaders.LOCATION);
            if (!location.startsWith(uri)) {
                context.fail(String.format("Location [%s] does not match uri [%s].", location, uri));
            }
            if (response.statusCode() != 201) {
                context.fail(String.format("Wrong status code [%d] received expected 201!", response.statusCode()));
            }
            async.complete();
        });
        request.exceptionHandler(exception -> {
            context.fail(exception.getLocalizedMessage());
            async.complete();
        });

        Thread.sleep(200);
        request.end(mapper.writeValueAsString(product));
        Thread.sleep(200);
    }
}
