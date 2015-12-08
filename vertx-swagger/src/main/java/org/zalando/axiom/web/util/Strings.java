package org.zalando.axiom.web.util;

import java.util.regex.Pattern;

public final class Strings {

    public static String camelToSnailCase(String fieldName) {
        if (fieldName == null) {
            return fieldName;
        }

        StringBuilder result = new StringBuilder(fieldName.length() * 2);
        for (char c : fieldName.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (result.length() > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static final Pattern SEGMENT_DELIMITER = Pattern.compile("/");

    /**
     * Converts a swagger templated path to a vertx-web templated path. From: /foo/{bar} to: /foo/:bar
     *
     * @param path swagger templated path
     * @return vertx templated path
     */
    public static String toVertxPathParams(String path) {
        if (path == null || "/".equals(path)) {
            return path;
        }
        String[] segments = SEGMENT_DELIMITER.split(path);

        StringBuilder result = new StringBuilder(segments.length * 2);
        for (String segment : segments) {
            if ("".equals(segment)) {
                continue;
            }
            result.append('/');
            if (segment.startsWith("{") && segment.endsWith("}")) {
                result.append(':').append(segment.substring(1, segment.length() - 1));
            } else {
                result.append(segment);
            }
        }
        return result.toString();
    }
}
