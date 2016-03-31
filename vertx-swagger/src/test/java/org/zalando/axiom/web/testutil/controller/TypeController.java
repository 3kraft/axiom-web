package org.zalando.axiom.web.testutil.controller;


import org.zalando.axiom.web.util.Preconditions;

import java.util.Date;

public class TypeController {

    public static final String RESULT = "ok";

    public String getByString(String id) {
        Preconditions.checkNotNull(id, "Should not be null");
        return RESULT;
    }

    public String getByInteger(int id) {
        return RESULT;
    }

    public String getByDouble(double id) {
        return RESULT;
    }

    public String getByFloat(float id) {
        return RESULT;
    }

    public String getByLong(long id) {
        return RESULT;
    }

    public String getByBoolean(boolean id) {
        return RESULT;
    }

    public String getByDate(Date date) {
        Preconditions.checkNotNull(date, "Should not be null");
        return String.valueOf(date.getTime()); //Comparing millis since epoch won't fail when test is run in different TZ
    }

}
