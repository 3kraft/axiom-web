package org.zalando.axiom.web.util;

import io.vertx.core.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.zalando.axiom.web.util.Strings.toMetricsName;
import static org.zalando.axiom.web.util.Strings.toVertxPathParams;

@RunWith(Parameterized.class)
public class ToMetricsNameTest {

    private final HttpMethod httpMethod;

    private final String path;

    private final String expectedName;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { HttpMethod.GET, "/foo/bar", "GET.foo.bar" },
                { HttpMethod.GET, "/foo/bar-quux", "GET.foo.bar-quux" },
                { HttpMethod.GET, "/foo/:bar/:quux", "GET.foo._bar._quux" },
                { HttpMethod.GET, "/foo/:bar/:quux", "GET.foo._bar._quux" },
                { HttpMethod.GET, "/foo/:bar/{quux}", "GET.foo._bar._quux" },
                { HttpMethod.POST, "/foo/bar", "POST.foo.bar" },
        });
    }

    public ToMetricsNameTest(HttpMethod httpMethod, String path, String expectedName) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.expectedName = expectedName;
    }

    @Test
    public void testToVertxPath() throws Exception {
        Assert.assertEquals(expectedName, toMetricsName(httpMethod, path));
    }
}