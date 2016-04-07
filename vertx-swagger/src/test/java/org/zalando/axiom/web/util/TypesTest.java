package org.zalando.axiom.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zalando.axiom.web.SwaggerRouter;
import org.zalando.axiom.web.testutil.ResponseConsumer;
import org.zalando.axiom.web.testutil.VertxTestBuilder;
import org.zalando.axiom.web.testutil.controller.TypeController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

@RunWith(VertxUnitRunner.class)
public class TypesTest {

    private static Vertx vertx;

    private static TypeController typeController = new TypeController();

    private static Router parameterRouter;

    @BeforeClass
    public static void setUp() {
        vertx = Vertx.vertx();

        // @formatter:off
        parameterRouter = SwaggerRouter.configure().swaggerDefinition("/swagger-parameter-test.json")
                .bindTo("/string-parameters")
                    .get((String value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByString(value)));
                    })
                    .doBind()
                .bindTo("/integer-parameters")
                    .get((Integer value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByInteger(value)));
                    })
                    .doBind()
                .bindTo("/long-parameters")
                    .get((Long value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByLong(value)));
                    })
                    .doBind()
                .bindTo("/double-parameters")
                    .get((Double value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByDouble(value)));
                    })
                    .doBind()
                .bindTo("/float-parameters")
                    .get((Float value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByFloat(value)));
                    })
                    .doBind()
                .bindTo("/boolean-parameters")
                    .get((Boolean value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByBoolean(value)));
                    })
                    .doBind()
                .bindTo("/date-parameters")
                    .get((Date value, AsyncResultHandler<String> handler) -> {
                        handler.handle(Future.succeededFuture(typeController.getByDate(value)));
                    })
                    .doBind()
                .router(vertx);
        // @formatter:on

    }

    @AfterClass
    public static void tearDown() throws Exception {
        vertx.close();
    }

    private ResponseConsumer getResponseTester(String successValue) {
        return (context, response) -> {
            response.bodyHandler(body -> {
                context.assertEquals(getJsonRepresentation(successValue), body.toString());
            });
        };
    }

    private String getJsonRepresentation(String successValue) {
        try {
            return new ObjectMapper().writeValueAsString(successValue);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getURLEncodedString(String inp) {
        try {
            return URLEncoder.encode(inp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testStringParameter(TestContext context) {
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/string-parameters?parameter=test")
                .responseAssertion(getResponseTester(TypeController.RESULT))
                .start(context, vertx);
    }

    @Test
    public void testIntegerParameter(TestContext context) {
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/integer-parameters?parameter=300")
                .responseAssertion(getResponseTester(TypeController.RESULT))
                .start(context, vertx);
    }

    @Test
    public void testIntegerParameterFail(TestContext context) {
        VertxTestBuilder.tester()
                .expectedStatusCode(500)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/integer-parameters?parameter=a300")
                .start(context, vertx);
    }

    @Test
    public void testLongParameter(TestContext context) {
        Long paramValue = Integer.MAX_VALUE + 10L;
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/long-parameters?parameter=" + paramValue)
                .responseAssertion(getResponseTester(TypeController.RESULT))
                .start(context, vertx);
    }

    @Test
    public void testBooleanParameter(TestContext context) {
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/boolean-parameters?parameter=true")
                .responseAssertion(getResponseTester(TypeController.RESULT))
                .start(context, vertx);
    }

    @Test
    public void testDoubleParameter(TestContext context) {
        Double paramValue = Float.MAX_VALUE + 10D;
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/double-parameters?parameter=" + paramValue)
                .responseAssertion(getResponseTester(TypeController.RESULT))
                .start(context, vertx);
    }

    @Test
    public void testFloatParameter(TestContext context) {
        Float paramValue = 30f;
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/float-parameters?parameter=" + paramValue)
                .responseAssertion(getResponseTester(TypeController.RESULT))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterISODate(TestContext context) {
        String dateParam = "1994-11-06";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784080000000"))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterISODatetime(TestContext context) {
        String dateParam = "1994-11-06T11:49:37.1+03:00";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784111777100"))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterISODatetimeTimezone(TestContext context) {
        String dateParam = "1994-11-06T08:49:37.123Z";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784111777123"))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterIMFDate(TestContext context) {
        String dateParam = "Sun, 06 Nov 1994 08:49:37 GMT";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784111777000"))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterRFC850(TestContext context) {
        String dateParam = "Sunday, 06-Nov-94 08:49:37 GMT";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784111777000"))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterASCTime(TestContext context) {
        String dateParam = "Sun Nov 6 08:49:37 1994";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784111777000"))
                .start(context, vertx);
    }

    @Test
    public void testDateParameterCustomType(TestContext context) {
        String datePattern = "yyyy MMM EEE d ss:mm:HH";
        Types.addFormatter(new DateFormatContainer(datePattern, DateFormatContainer.DEFAULT_TIME_ZONE, (value) -> value.length() == 23));

        String dateParam = "1994 Nov Sun 6 37:49:08";
        VertxTestBuilder.tester()
                .expectedStatusCode(200)
                .routerFactory(() -> parameterRouter)
                .getRequest("/v1/date-parameters?parameter=" + getURLEncodedString(dateParam))
                .responseAssertion(getResponseTester("784111777000"))
                .start(context, vertx);
    }


}
