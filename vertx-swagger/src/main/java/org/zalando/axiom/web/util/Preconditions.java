package org.zalando.axiom.web.util;

public final class Preconditions {

    private Preconditions() {
    }

    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }
}
