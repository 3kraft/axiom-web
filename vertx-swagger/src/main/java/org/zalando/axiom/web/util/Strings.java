package org.zalando.axiom.web.util;

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

}
