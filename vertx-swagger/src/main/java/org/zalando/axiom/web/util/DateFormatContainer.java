package org.zalando.axiom.web.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

/**
 * Use constructors for creating an instance.<br>
 * An instance can have either a format string + timezone combination or a formatter object.<br>
 * <br>
 * The quickTest will be used as an optimization to speed up pattern checks while attempting conversion<br>
 */
public class DateFormatContainer {

    private static final Predicate<String> ALWAYS_TRUE = (value) -> true;

    public static final String DEFAULT_TIME_ZONE = "UTC";

    public static final String TIMEZONE_IN_PATTERN = "T_I-P";

    private String formatString;

    private String timeZone;

    private Predicate<String> quickTest;

    private DateTimeFormatter formatter;

    /**
     * These are compiled to {@link DateTimeFormatter}. If timezone is in format string, use {@link #TIMEZONE_IN_PATTERN}.<br>
     * You could also use {@link #DEFAULT_TIME_ZONE} for the "UTC"
     *
     * @param formatString Pattern compatible with java.time.format.DateTimeFormatter
     * @param timeZone     Value compatible with  java.time.ZoneId#of
     */
    public DateFormatContainer(String formatString, String timeZone) {
        this(formatString, timeZone, ALWAYS_TRUE);
    }

    /**
     * These are compiled to {@link DateTimeFormatter}. If timezone is in format string, use {@link #TIMEZONE_IN_PATTERN}.<br>
     * You could also use {@link #DEFAULT_TIME_ZONE} for the "UTC"
     *
     * @param formatString Pattern compatible with java.time.format.DateTimeFormatter
     * @param timeZone     Value compatible with  java.time.ZoneId#of
     * @param quickTest    Takes in the value to be parsed as input. If the answer is false, the value is not parsed as per this format.
     */
    public DateFormatContainer(String formatString, String timeZone, Predicate<String> quickTest) {
        Preconditions.checkNotBlank(formatString, "Format string cannot be null");
        Preconditions.checkNotBlank(timeZone, "Time zone cannot be null. Use DEFAULT_TIME_ZONE or TIMEZONE_IN_PATTERN");
        Preconditions.checkNotNull(quickTest, "Test cannot be null");
        this.formatString = formatString;
        this.timeZone = timeZone;
        this.quickTest = quickTest;
        this.formatter = generateFormatter(formatString, timeZone);
    }

    public DateFormatContainer(Predicate<String> quickTest, DateTimeFormatter formatter) {
        Preconditions.checkNotNull(quickTest, "Test cannot be null");
        Preconditions.checkNotNull(formatter, "Formatter cannot be null while using this constructor");
        this.quickTest = quickTest;
        this.formatter = formatter;
    }

    private static DateTimeFormatter generateFormatter(String formatString, String timeZone) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatString);
        if (!TIMEZONE_IN_PATTERN.equals(timeZone)) {
            dtf = dtf.withZone(ZoneId.of(timeZone));
        }
        return dtf;
    }

    public String getFormatString() {
        return formatString;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Predicate<String> getQuickTest() {
        return quickTest;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    @Override
    public String toString() {
        return "DateFormatContainer{" +
                "formatString='" + formatString + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", quickTest=" + quickTest +
                ", formatter=" + formatter +
                '}';
    }
}
