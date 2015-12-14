package org.zalando.axiom.web.util;

public final class Preconditions {

    private Preconditions() {
    }

    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }

    public static void checkNotBlank(String string, String message) {
        if (string == null || "".equals(string)) {
            throw new IllegalArgumentException(message);
        }
    }
}
