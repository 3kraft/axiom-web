package org.zalando.axiom.web.util;

import io.swagger.models.parameters.QueryParameter;

public final class Types {

    private Types() {
    }

    public static Object castValueToType(String value, Class<?> parameterType) {
        if (parameterType == double.class) {
            return Double.parseDouble(value);
        } else if (parameterType == int.class) {
            return Integer.parseInt(value);
        } else if (parameterType == float.class) {
            return Float.parseFloat(value);
        } else if (parameterType == long.class) {
            return Long.parseLong(value);
        } else if (parameterType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            throw new UnsupportedOperationException(String.format("Unhandled type [%s].", parameterType.getName()));
        }
    }

    public static Class<?> getParameterType(QueryParameter queryParameter) {
        switch (queryParameter.getType()) {
            case "number":
                switch (queryParameter.getFormat()) {
                    case "integer":
                        return int.class;
                    case "long":
                        return long.class;
                    case "float":
                        return float.class;
                    case "double":
                        return double.class;
                    default:
                        return int.class;
                }
            case "integer":
                switch (queryParameter.getFormat()) {
                    case "integer":
                        return int.class;
                    case "long":
                        return long.class;
                    default:
                        return int.class;
                }
            case "string":
                return String.class;
            case "boolean":
                return boolean.class;
            default:
                throw new UnsupportedOperationException(String.format("Type [%s] format [%s] not handled.", queryParameter.getType(), queryParameter.getFormat()));
        }
    }
}
