package org.zalando.axiom.web.util;

import sun.reflect.ConstantPool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.function.Function;

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
        } else if (parameterType == String.class) {
            return value;
        }  else {
            throw new UnsupportedOperationException(String.format("Unhandled type [%s].", parameterType.getName()));
        }
    }

    public static Class<?> getParameterType(String type, String format) {
        switch (type) {
            case "number":
                switch (format) {
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
                switch (format) {
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
                throw new UnsupportedOperationException(String.format("Type [%s] format [%s] not handled.", type, format));
        }
    }
}
