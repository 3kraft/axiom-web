package org.zalando.axiom.web.util;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.DateTimeParserBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Optional;

public final class Types {

    private Types() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Types.class);

    // http://tools.ietf.org/html/rfc7231#section-7.1.1.1
    private static final String IMF_FIX_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String RFC_850_DATE_FORMAT = "EEEE, dd-MMM-yy HH:mm:ss 'GMT'";
    private static final String ASCTIME_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";

    //http://tools.ietf.org/html/rfc3339#section-5.6
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String ZONEID_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String ZONEID_DATE_TIME_MS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    //These two are mostly useless
    private static final String ZONENAME_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";
    private static final String ZONENAME_DATE_TIME_MS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSz";

    public static final String DEFAULT_TIME_ZONE = "UTC";
    public static final String TIMEZONE_IN_PATTERN = "T_I-P";

    private static LinkedHashMap<String, String> dateTimePatterns = new LinkedHashMap<>();

    static {
        dateTimePatterns.put(ZONEID_DATE_TIME_MS_FORMAT, TIMEZONE_IN_PATTERN);
        dateTimePatterns.put(ZONEID_DATE_TIME_FORMAT, TIMEZONE_IN_PATTERN);
        dateTimePatterns.put(ZONENAME_DATE_TIME_MS_FORMAT, TIMEZONE_IN_PATTERN);
        dateTimePatterns.put(ZONENAME_DATE_TIME_FORMAT, TIMEZONE_IN_PATTERN);
        dateTimePatterns.put(DATE_FORMAT, DEFAULT_TIME_ZONE);

        dateTimePatterns.put(IMF_FIX_DATE_FORMAT, DEFAULT_TIME_ZONE);
        dateTimePatterns.put(RFC_850_DATE_FORMAT, DEFAULT_TIME_ZONE);
        dateTimePatterns.put(ASCTIME_DATE_FORMAT, DEFAULT_TIME_ZONE);
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
                    .map(pattern -> {
                        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
                        String timeZone = dateTimePatterns.get(pattern);
                        if (!TIMEZONE_IN_PATTERN.equals(timeZone)) {
                            formatter = formatter.withZone(DateTimeZone.forID(timeZone));
                        }

                        Optional<Date> op = tryParse(formatter, value.trim());
                        if (op.isPresent()) {
                            LOGGER.debug("Using {} for {}", pattern, value);
                        }
                        return op;
                    })
                    .filter(Optional::isPresent).map(matchingDate -> matchingDate.get()).findFirst();
            return date.orElseThrow(() -> new IllegalArgumentException("Date format not supported"));
        } else {
            throw new UnsupportedOperationException(String.format("Unhandled type [%s].", parameterType.getName()));
        }
    }

    /**
     * Copy of {@link DateTimeFormatter#parseDateTime(String)} , which returns Optional<Date> instead of throwing an exception.
     * SimpleDateFormat cannot be used because it has issues : http://stackoverflow.com/a/7160024
     *
     * @param formatter
     * @param text
     * @return
     */
    private static Optional<Date> tryParse(DateTimeFormatter formatter, String text) {
        DateTimeParser parser = formatter.getParser();

        Chronology chrono = selectChronology(formatter.getChronology(), formatter.getZone());
        DateTimeParserBucket bucket = new DateTimeParserBucket(0, chrono, formatter.getLocale(), formatter.getPivotYear(), formatter.getDefaultYear());
        int newPos = parser.parseInto(bucket, text, 0);
        if (newPos >= 0 && newPos >= text.length()) {
            long millis = bucket.computeMillis(true, text);
            if (formatter.isOffsetParsed() && bucket.getOffsetInteger() != null) {
                int parsedOffset = bucket.getOffsetInteger();
                DateTimeZone parsedZone = DateTimeZone.forOffsetMillis(parsedOffset);
                chrono = chrono.withZone(parsedZone);
            } else if (bucket.getZone() != null) {
                chrono = chrono.withZone(bucket.getZone());
            }
            DateTime dt = new DateTime(millis, chrono);
            if (formatter.getZone() != null) {
                dt = dt.withZone(formatter.getZone());
            }
            return Optional.of(dt.toDate());
        } else {
            return Optional.empty();
        }
    }

    private static Chronology selectChronology(Chronology configuredChrono, DateTimeZone configuredZone) {
        Chronology defaultChrono = DateTimeUtils.getChronology(null);
        if (configuredChrono != null) {
            defaultChrono = configuredChrono;
        }
        if (configuredZone != null) {
            defaultChrono = defaultChrono.withZone(configuredZone);
        }
        return defaultChrono;
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
     * @param formatString Pattern string compliant with http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
     */
    public static final void addFormatterWithDefaultTimezone(String formatString) {
        dateTimePatterns.put(formatString, DEFAULT_TIME_ZONE);
    }

    /**
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param formatString Pattern string compliant with http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
     */
    public static final void addFormatterWithTimezoneInPattern(String formatString) {
        dateTimePatterns.put(formatString, TIMEZONE_IN_PATTERN);
    }

    /**
     * Timezone should be valid timezone string as per {@link DateTimeZone}
     * Should be added from most specific to least specific.
     * Formats are checked in order of insertion, after pre-configured formats
     * <p>
     * This method is not thread-safe.
     *
     * @param formatString Pattern string compliant with http://joda-time.sourceforge.net/apidocs/org/joda/time/format/DateTimeFormat.html
     * @param timeZone     Timezone compliant with {@link DateTimeZone#forID(String)}
     */
    public static final void addFormatter(String formatString, String timeZone) {
        dateTimePatterns.put(formatString, timeZone);
    }

    /**
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param formatStrings Map of format strings to timezones. Preconfigured {@link #DEFAULT_TIME_ZONE} or {@link #TIMEZONE_IN_PATTERN}
     */
    public static final void setDateFormatters(LinkedHashMap<String, String> formatStrings) {
        dateTimePatterns.clear();
        dateTimePatterns.putAll(formatStrings);
    }

    public static final LinkedHashMap<String, String> getFormats() {
        return (LinkedHashMap<String, String>) dateTimePatterns.clone();
    }
}
