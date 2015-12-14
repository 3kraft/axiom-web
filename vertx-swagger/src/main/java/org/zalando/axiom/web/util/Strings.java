package org.zalando.axiom.web.util;

import io.vertx.core.http.HttpMethod;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.zalando.axiom.web.util.Preconditions.checkNotBlank;
import static org.zalando.axiom.web.util.Preconditions.checkNotNull;

public final class Strings {

    private static final Pattern SEGMENT_DELIMITER = Pattern.compile("/");

    public static String getSetterName(String name) {
        checkNotNull("Name must not be null!", name);
        if (name.length() == 0) {
            throw new IllegalArgumentException("Name must not be blank!");
        }
        StringBuilder result = new StringBuilder(3);
        result.append("set").append(name.substring(0, 1).toUpperCase());
        if (name.length() > 1) {
            result.append(name.substring(1));
        }
        return result.toString();
    }

    public static String camelToSnailCase(String fieldName) {
        if (fieldName == null) {
            return null;
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

    /**
     * Converts a swagger path with templates to the vertx-web template format. From: /foo/{bar} to: /foo/:bar
     *
     * @param path swagger path
     * @return vertx path
     */
    public static String toVertxPathParams(String path) {
        return pathTransformer(path,
                segment -> segment.startsWith("{") && segment.endsWith("}"),
                segment -> ":" + segment.substring(1, segment.length() - 1));
    }

    /**
     * Converts a vertx-web path with templates to the swagger path template format. From: /foo/:bar to: /foo/{bar}
     *
     * @param path swagger path
     * @return vertx path
     */
    public static String toSwaggerPathParams(String path) {
        return pathTransformer(path,
                segment -> segment.startsWith(":"),
                segment -> "{" + segment.substring(1) + "}");
    }

    public static boolean isBlank(String string) {
        return string == null || "".equals(string);
    }

    public static String valueOrElse(String value, String fallback) {
        return isBlank(value) ? "" : value;
    }

    private static String pathTransformer(String path, Predicate<String> txPredicate, Function<String, String> txFunction) {
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
            if (txPredicate.test(segment)) {
                result.append(txFunction.apply(segment));
            } else {
                result.append(segment);
            }
        }
        return result.toString();
    }

    private static final Pattern METRICS_REPLACE_PATTERN = Pattern.compile("[{}:]");
    // "zmon.response.200.GET.checks.all-active-check-definitions.count": 10,
    public static String toMetricsName(HttpMethod httpMethod, String path) {
        checkNotNull(httpMethod, "Http method must not be null!");
        checkNotBlank(path, "Path must not be blank!");

        return httpMethod.name() + METRICS_REPLACE_PATTERN.matcher(path.replace('/', '.').replace("}", "")).replaceAll("_");
    }
}
