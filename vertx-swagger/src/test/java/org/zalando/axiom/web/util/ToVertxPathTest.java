package org.zalando.axiom.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ToVertxPathTest {

    private String swaggerPath;

    private String vertxPath;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "/foo/bar", "/foo/bar" },
                { "/", "/" },
                { "/foo/{bar}", "/foo/:bar"},
                { "/foo/{bar}/quuz/{baz}", "/foo/:bar/quuz/:baz"},
                {"", ""},
                {null, null}
        });
    }

    public ToVertxPathTest(String swaggerPath, String vertxPath) {
        this.swaggerPath = swaggerPath;
        this.vertxPath = vertxPath;
    }

    @Test
    public void testToVertxPath() throws Exception {
        Assert.assertEquals(vertxPath, Strings.toVertxPathParams(swaggerPath));

    }
}