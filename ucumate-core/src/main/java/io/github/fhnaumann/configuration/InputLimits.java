package io.github.fhnaumann.configuration;

import java.util.Properties;

/**
 * Upper bounds for inputs that are processed by {@link io.github.fhnaumann.funcs.UCUMService}. Inputs exceeding them
 * are rejected with a failure result instead of being processed. This protects callers that process untrusted input
 * (i.e. a validation server) from inputs that cause unbounded computation time or a {@link StackOverflowError}.
 * <br>
 * The defaults are far above anything found in real-world UCUM codes. Use {@link #unlimited()} or set
 * <code>ucumate.limits.enable=false</code> to process any input regardless of its cost.
 *
 * @param enable Whether the limits are enforced at all.
 * @param maxUnitLength The maximum length of a unit code. Text within annotations (<code>{...}</code>) is not counted.
 *                      The default of 1000 is safe for the JVM default thread stack size (1 MB). Lower it if units are
 *                      processed on threads with a smaller stack.
 * @param maxTotalExponent The maximum sum of the absolute exponents of all components in a unit. A component without an
 *                         exponent counts as 1. I.e. <code>10*-3.m2/s</code> has a total exponent of 6.
 * @param maxValueDigits The maximum number of digits of a numeric value (the conversion/canonicalization factor and the
 *                       molar mass coefficient), when written without scientific notation. I.e. <code>5e3</code> has 4 digits.
 * @author Felix Naumann
 */
public record InputLimits(boolean enable, int maxUnitLength, int maxTotalExponent, int maxValueDigits) {

    public static final int DEFAULT_MAX_UNIT_LENGTH = 1000;
    public static final int DEFAULT_MAX_TOTAL_EXPONENT = 300;
    public static final int DEFAULT_MAX_VALUE_DIGITS = 1000;

    public static InputLimits defaults() {
        return new InputLimits(true, DEFAULT_MAX_UNIT_LENGTH, DEFAULT_MAX_TOTAL_EXPONENT, DEFAULT_MAX_VALUE_DIGITS);
    }

    /**
     * @return Limits that are never enforced. This is the behaviour of ucumate before the limits were introduced.
     */
    public static InputLimits unlimited() {
        return new InputLimits(false, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static InputLimits fromProps(Properties properties) {
        boolean enable = Boolean.parseBoolean(properties.getProperty("ucumate.limits.enable", "true"));
        int maxUnitLength = Integer.parseInt(properties.getProperty("ucumate.limits.maxUnitLength", Integer.toString(DEFAULT_MAX_UNIT_LENGTH)));
        int maxTotalExponent = Integer.parseInt(properties.getProperty("ucumate.limits.maxTotalExponent", Integer.toString(DEFAULT_MAX_TOTAL_EXPONENT)));
        int maxValueDigits = Integer.parseInt(properties.getProperty("ucumate.limits.maxValueDigits", Integer.toString(DEFAULT_MAX_VALUE_DIGITS)));
        return new InputLimits(enable, maxUnitLength, maxTotalExponent, maxValueDigits);
    }

    public Properties asProps() {
        Properties props = new Properties();
        props.put("ucumate.limits.enable", Boolean.toString(enable));
        props.put("ucumate.limits.maxUnitLength", Integer.toString(maxUnitLength));
        props.put("ucumate.limits.maxTotalExponent", Integer.toString(maxTotalExponent));
        props.put("ucumate.limits.maxValueDigits", Integer.toString(maxValueDigits));
        return props;
    }
}
