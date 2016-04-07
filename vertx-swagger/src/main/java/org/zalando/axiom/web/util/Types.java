package org.zalando.axiom.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public final class Types {

    private Types() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Types.class);

    private static List<DateFormatContainer> dateFormats = new ArrayList<>();


    // http://tools.ietf.org/html/rfc7231#section-7.1.1.1
    private static final String IMF_FIX_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String ASCTIME_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";

    private static final DateTimeFormatter RFC_850_DATE_FORMATTER;

    //http://tools.ietf.org/html/rfc3339#section-5.6
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    static {
        RFC_850_DATE_FORMATTER = new DateTimeFormatterBuilder().
                appendPattern("EEEE, dd-MMM-")
                .appendValueReduced(ChronoField.YEAR, 2, 2, LocalDate.of(1900, 1, 1))
                .appendPattern(" HH:mm:ss 'GMT'")
                .toFormatter()
                .withResolverStyle(ResolverStyle.STRICT)
                .withZone(ZoneId.of("UTC"));

        dateFormats.add(new DateFormatContainer(IMF_FIX_DATE_FORMAT, DateFormatContainer.DEFAULT_TIME_ZONE, value -> value.length() == 29));
        dateFormats.add(new DateFormatContainer(ASCTIME_DATE_FORMAT, DateFormatContainer.DEFAULT_TIME_ZONE, value -> value.length() == 23));
        dateFormats.add(new DateFormatContainer(value -> value.length() >= 30, RFC_850_DATE_FORMATTER));
        dateFormats.add(new DateFormatContainer(value -> value.length() == 28 || value.length() == 29, DateTimeFormatter.RFC_1123_DATE_TIME));

        dateFormats.add(new DateFormatContainer(DATE_FORMAT, DateFormatContainer.DEFAULT_TIME_ZONE, value -> value.length() == 10));
        dateFormats.add(new DateFormatContainer(value -> value.length() > 20, DateTimeFormatter.ISO_ZONED_DATE_TIME));
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
            Optional<Date> date = dateFormats.stream().
                    map(dateFormatContainer -> {
                        if (!dateFormatContainer.getQuickTest().test(value)) {
                            LOGGER.debug("Quick test failed. Skipping {} for {}", dateFormatContainer, value);
                            return Optional.<Date>empty();
                        }
                        try {
                            DateTimeFormatter dtf = dateFormatContainer.getFormatter();
                            TemporalAccessor accessor = dtf.parse(value);
                            LocalDate ld = accessor.query(TemporalQueries.localDate());
                            LocalTime lt = accessor.query(TemporalQueries.localTime());
                            ZoneId zoneId = accessor.query(TemporalQueries.zone());
                            if (zoneId == null) {
                                zoneId = dtf.getZone();
                            }
                            ZonedDateTime zonedDateTime = null;
                            if (lt == null) {
                                zonedDateTime = ld.atStartOfDay(zoneId);
                            } else {
                                zonedDateTime = ZonedDateTime.of(ld, lt, zoneId);
                            }
                            LOGGER.debug("Using {} formatter for {}", dateFormatContainer, value);
                            return Optional.of(Date.from(zonedDateTime.toInstant()));
                        } catch (Exception e) {
                            LOGGER.error("Caught error parsing {} with formatter {}. Consider changing the quickTest for better performance", value, dateFormatContainer, e);
                            return Optional.<Date>empty();
                        }
                    })
                    .filter(Optional::isPresent).map(matchingDate -> matchingDate.get()).findFirst();
            return date.orElseThrow(() -> new IllegalArgumentException("Date format not supported"));
        } else {
            throw new UnsupportedOperationException(String.format("Unhandled type [%s].", parameterType.getName()));
        }
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
     * Formats are checked in order of insertion.<br>
     * User "quickTest" to reduce number of parsing exceptions thrown<br>
     *
     * @param format Additional formatter to parse date string.
     */
    public static void addFormatter(DateFormatContainer format) {
        dateFormats.add(format);
    }

    /**
     * This will replace preconfigured formatters
     * <p>
     * Should be added from most specific to least specific.<br>
     * Formats are checked in order of insertion.
     *
     * @param dateFormatters List of formatters
     */
    public static void setDateFormatters(List<DateFormatContainer> dateFormatters) {
        dateFormats.clear();
        dateFormats.addAll(dateFormatters);
    }

    /**
     * This will be useful if order of preconfigured formatters is to be changed.
     *
     * @return Copy of date formatters
     */
    public static List<DateFormatContainer> getFormats() {
        return new ArrayList<>(dateFormats);
    }

}
