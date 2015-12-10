package org.zalando.axiom.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class GetSetterNameTest {

    private final String name;

    private final String setterName;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "fooBar", "setFooBar" },
                { "ACME", "setACME"},
                {"a", "setA"}
        });
    }

    public GetSetterNameTest(String name, String setterName) {
        this.name = name;
        this.setterName = setterName;
    }

    @Test
    public void testGetSetterName() throws Exception {
        Assert.assertEquals(Strings.getSetterName(name), setterName);
    }
}