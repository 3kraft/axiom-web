package org.zalando.axiom.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class CamelToSnailCaseTest {

    private final String camel;

    private final String snail;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "fooBar", "foo_bar" },
                { "fooBarQuux", "foo_bar_quux" },
                { "ACME", "a_c_m_e"},
                {"", ""},
                {null, null}
        });
    }

    public CamelToSnailCaseTest(String camel, String snail) {
        this.camel = camel;
        this.snail = snail;
    }

    @Test
    public void testCamelToSnail() throws Exception {
        Assert.assertEquals(Strings.camelToSnailCase(camel), snail);

    }
}