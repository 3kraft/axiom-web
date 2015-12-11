package org.zalando.axiom.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.zalando.axiom.web.util.Strings.toSwaggerPathParams;

@RunWith(Parameterized.class)
public class ToSwaggerPathTest {

    private String swaggerPath;

    private String vertxPath;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "/foo/bar", "/foo/bar" },
                { "/", "/" },
                { "/foo/{bar}", "/foo/:bar"},
                { "/foo/{bar}/quuz/{baz}", "/foo/:bar/quuz/:baz"},
                { "/foo/{bar}/quuz/{baz}", "/foo/:bar/quuz/{baz}"},
                {"", ""},
                {null, null}
        });
    }

    public ToSwaggerPathTest(String swaggerPath, String vertxPath) {
        this.swaggerPath = swaggerPath;
        this.vertxPath = vertxPath;
    }

    @Test
    public void testToSwaggerPath() throws Exception {
        Assert.assertEquals(swaggerPath, toSwaggerPathParams(vertxPath));

    }
}