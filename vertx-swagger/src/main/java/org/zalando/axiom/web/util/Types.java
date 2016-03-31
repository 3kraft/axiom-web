package org.zalando.axiom.web.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

public final class Types {

    private Types() {
    }

    // http://tools.ietf.org/html/rfc7231#section-7.1.1.1
    private static final String IMF_FIX_DATE_FORMATTER = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String RFC_850_DATE_FORMATTER = "EEEE, dd-MMM-yy HH:mm:ss 'GMT'";
    private static final String ASCTIME_DATE_FORMATTER = "EEE MMM d HH:mm:ss yyyy";
    //
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ZONED_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String ZONED_DATE_TIME_NS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final String DEFAULT_TIME_ZONE = "UTC";
    private static final String TIMEZONE_IN_PATTERN = "T_I-P";

    private static Map<String, String> dateTimePatterns = new LinkedHashMap<>();    //Because we must process from most specific to least specific format

    static {
        dateTimePatterns.put(ZONED_DATE_TIME_NS_FORMAT, TIMEZONE_IN_PATTERN);
        dateTimePatterns.put(ZONED_DATE_TIME_FORMAT, TIMEZONE_IN_PATTERN);
        dateTimePatterns.put(DATE_FORMAT, DEFAULT_TIME_ZONE);

        dateTimePatterns.put(IMF_FIX_DATE_FORMATTER, DEFAULT_TIME_ZONE);
        dateTimePatterns.put(RFC_850_DATE_FORMATTER, DEFAULT_TIME_ZONE);
        dateTimePatterns.put(ASCTIME_DATE_FORMATTER, DEFAULT_TIME_ZONE);
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
        } else if (parameterType == Date.class) {
            Preconditions.checkNotNull(value, "Cannot be null");
            Optional<Date> date = dateTimePatterns.keySet().stream()
                    .map(pattern -> attemptConversionToDate(pattern, value.trim()))
                    .filter(convertedDate -> convertedDate != null).findFirst();
            return date.orElseThrow(() -> new IllegalArgumentException("Date format not supported"));
        } else {
            throw new UnsupportedOperationException(String.format("Unhandled type [%s].", parameterType.getName()));
        }
    }

    private static Date attemptConversionToDate(String pattern, String value) {
        String timeZone = dateTimePatterns.get(pattern);
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        if (!TIMEZONE_IN_PATTERN.equals(timeZone)) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        }
        return dateFormat.parse(value, new ParsePosition(0));
    }

    public static Class<?> getParameterType(String type, String format) {
        switch (type) {
            case "number":
                if (format == null) {
                    return int.class;
                }
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
                if (format == null) {
                    return int.class;
                }
                switch (format) {
                    case "int32":
                        return int.class;
                    case "int64":
                        return long.class;
                    default:
                        return int.class;
                }
            case "string":
                if (format == null) {
                    return String.class;
                }
                switch (format) {
                    case "date":
                    case "datetime":
                        return Date.class;
                    default:
                        return String.class;
                }
            case "boolean":
                return boolean.class;
            default:
                throw new UnsupportedOperationException(String.format("Type [%s] format [%s] not handled.", type, format));
        }
    }

    /**
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param formatString
     */
    public static final void addFormatterWithDefaultTimezone(String formatString) {
        dateTimePatterns.put(formatString, DEFAULT_TIME_ZONE);
    }

    /**
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param formatString
     */
    public static final void addFormatterWithTimezoneInPattern(String formatString) {
        dateTimePatterns.put(formatString, TIMEZONE_IN_PATTERN);
    }

    /**
     * Timezone should be valid timezone string as per {@link TimeZone#getTimeZone} <br>
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param formatString
     * @param timeZone
     */
    public static final void addFormatter(String formatString, String timeZone) {
        dateTimePatterns.put(formatString, timeZone);
    }

    /**
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param formatStrings
     */
    public static final void setDateFormatters(LinkedHashMap<String, String> formatStrings) {
        dateTimePatterns.clear();
        dateTimePatterns.putAll(formatStrings);
    }
}
