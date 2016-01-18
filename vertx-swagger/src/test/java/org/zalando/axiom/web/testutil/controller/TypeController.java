package org.zalando.axiom.web.testutil.controller;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TypeController {

    private static final List<String> RESULT = Arrays.asList("ok");

    public Collection<String> getByString(String id) {
        return RESULT;
    }

    public Collection<String> getByInteger(int id) { return RESULT; }

    public Collection<String> getByDouble(double id) { return RESULT; }

    public Collection<String> getByFloat(float id) { return RESULT; }

    public Collection<String> getByLong(long id) { return RESULT; }

    public Collection<String> getByBoolean(boolean id) { return RESULT; }

}
