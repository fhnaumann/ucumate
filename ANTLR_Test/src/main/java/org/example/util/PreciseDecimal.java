package org.example.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A precision‐aware decimal implementation for UCUM canonicalization.
 *
 * Instances are created from a String value. If the String contains a
 * decimal point, we treat the number as having “limited precision” and we
 * record both its scale (number of digits after the decimal) and the total
 * number of significant digits (as represented in the input).
 *
 * Whole numbers (without a decimal point) are considered exact and have
 * unlimited precision.
 *
 * <p>Arithmetic operations are implemented so that:
 * <ul>
 *   <li>For addition and subtraction, if one operand has limited precision,
 *       the result is rounded to that many fractional digits.
 *   <li>For multiplication and division, if either operand is limited,
 *       the result’s precision is set to the minimum (treating unlimited as infinite).
 * </ul>
 *
 * <p>Example behaviors:
 * <pre>
 *   new PreciseDecimal("2")     [exact]      // unlimited precision: prints "2"
 *   new PreciseDecimal("2.0")   [limited: scale = 1, precision = 2] // prints "2.0"
 *
 *   // Addition: whole number is exact, so result uses the finite operand’s scale.
 *   new PreciseDecimal("2").add(new PreciseDecimal("0.001"))  // yields 2.001
 *
 *   // Addition: if the finite operand “2.0” (one fractional digit) is used,
 *   // the result rounds to one decimal place:
 *   new PreciseDecimal("2.0").add(new PreciseDecimal("0.001"))  // yields 2.0
 *
 *   // Multiplication: limited precision numbers lead to a result whose
 *   // significant figures are the minimum of the two. For example,
 *   // "2.0" (2 sig digits) multiplied by "2.00" (3 sig digits) yields "4.0"
 *   new PreciseDecimal("2.0").multiply(new PreciseDecimal("2.00"))
 *       .toString();  // yields "4.0"
 * </pre>
 */
public class PreciseDecimal {

    // The underlying BigDecimal value.
    private final BigDecimal value;
    // Flag: if true the input was given with a decimal point (i.e. has limited precision)
    private final boolean limited;
    // For numbers with limited precision, the number of significant digits (as parsed from the input).
    // For example, "2.0" → 2; "2.00" → 3; "0.001" → 1 (since leading zeros in the fractional part are not significant).
    private final int precision;
    // For numbers with limited precision, the scale (number of digits to the right of the decimal point).
    private final int scale;

    public static PreciseDecimal ONE = new PreciseDecimal("1");

    /**
     * Constructs a new PreciseDecimal from a string.
     * If the string contains a decimal point then the number is considered limited,
     * and its scale and significant digit count are derived from the string.
     * Whole numbers (no decimal point) are marked as exact (unlimited).
     *
     * @param s
     *         the string representation of the number (e.g. "2.0", "0.001", "2")
     *
     * @throws IllegalArgumentException
     *         if the string is not a valid decimal.
     */
    public PreciseDecimal(String s) {
        if(s == null) {
            throw new IllegalArgumentException("Input string cannot be null");
        }
        s = s.trim();
        // Check if the number is negative; ignore the leading '-' for parsing the digit counts.
        boolean isNegative = s.startsWith("-");
        String abs = isNegative ? s.substring(1) : s;
        this.value = new BigDecimal(s);
        // Check for scientific notation
        if(abs.contains("e") || abs.contains("E")) {
            String[] parts = abs.toLowerCase().split("e");
            String mantissa = parts[0];
            if(mantissa.contains(".")) {
                // "1.0e3" or "2.54e-2" → limited precision
                this.limited = true;
                // String clean = mantissa.replace(".", "").replaceFirst("^0+", "");
                this.precision = countSignificantDigits(mantissa);
                this.scale = countScale(mantissa, Integer.parseInt(parts[1]));
            }
            else {
                // "1e3", "254e-2" → exact
                this.limited = false;
                this.precision = 0;
                this.scale = 0;
            }
        }
        else if(abs.contains(".")) {
            this.limited = true;
            int decIndex = abs.indexOf(".");
            // Scale is the count of characters after the decimal point.
            this.scale = abs.length() - decIndex - 1;
            // Compute significant digits from the original string.
            // We remove the decimal point.
            String digits = abs.replace(".", "");
            // In numbers less than 1, leading zeros immediately after the decimal are not significant.
             if(abs.startsWith("0") && decIndex == 1) {
                // Remove any leading zeros in the fractional part.
                String trimmed = abs.substring(decIndex + 1).replaceFirst("^0+", "");
                // If the number is 0 (like "0.000"), treat it as one significant digit.
                this.precision = trimmed.isEmpty() ? 1 : trimmed.length();
            } else {
                // For numbers with a non-zero integer part,
                // all digits (integer and fractional) are significant.
                this.precision = digits.length();
            }
        }
        else {
            // Whole numbers: mark as unlimited (exact).
            this.limited = false;
            this.precision = 0; // Not used for unlimited numbers.
            this.scale = 0;
        }
    }

    private int countSignificantDigits(String s) {
        String digitsOnly = s.replace(".", "");

        // Remove leading zeros
        String stripped = digitsOnly.replaceFirst("^0+", "");

        // If the result is empty (e.g., "0.000"), treat it as infinite sig fig
        return stripped.isEmpty() ? -1 : stripped.length();
        /*
        int dotIdx = s.indexOf(".");
        if(dotIdx == -1) {
            // i.e. 1e3, 5e3, 560e6
            // all parts are significant
            return s.length();
        }
        else {
            // i.e. 1.0e3, 2.54e5
            int excludeLeadingZeros = 0;
            boolean leadingZero = true;
            for(int i=0; i<s.length(); i++) {
                if(i == dotIdx) {
                    continue;
                }
                if(leadingZero && s.charAt(i) == '0') {
                    continue;
                }
                leadingZero = false;
                excludeLeadingZeros++;
            }
            return excludeLeadingZeros;
        }

         */
    }

    private int countScale(String s, int exponent) {
        int dotIndex = s.indexOf('.');
        if (dotIndex == -1) {
            return 0; // no decimal point = scale 0
        }

        // shift the dot in s exponent-many times to the right.
        // The remaining digits to the right are the remaining scale.
        int shiftedDotIdx = dotIndex + exponent - 1;
        int numberOfDigitsAfterDot = s.length() - dotIndex - 1;
        if(shiftedDotIdx >= numberOfDigitsAfterDot) {
            // even more zeros are necessary -> it's an integer, not a fraction
            return 0;
        }
        return numberOfDigitsAfterDot - shiftedDotIdx;
    }

    // Private constructor for internal operations.
    // Callers must provide the value, a flag whether it is limited, and (if limited) its precision and scale.
    private PreciseDecimal(BigDecimal value, boolean limited, int precision, int scale) {
        this.value = value;
        this.limited = limited;
        this.precision = precision;
        this.scale = scale;
    }

    /**
     * Returns the underlying BigDecimal value.
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Returns true if the number was constructed with limited (fixed) precision.
     */
    public boolean isLimited() {
        return limited;
    }

    /**
     * For limited values, returns the number of significant digits.
     * Returns -1 if the value is exact (unlimited).
     */
    public int getPrecision() {
        return limited ? precision : -1;
    }

    /**
     * For limited values, returns the scale (digits to the right of the decimal).
     */
    public int getScale() {
        return limited ? scale : 0;
    }

    /**
     * Adds this number to another.
     * For addition (and subtraction), we use the rule that if either operand has limited precision,
     * the result is rounded to the least number of fractional digits among those operands.
     *
     * @param other
     *         the other PreciseDecimal to add.
     *
     * @return a new PreciseDecimal representing the sum.
     */
    public PreciseDecimal add(PreciseDecimal other) {
        int newScale;
        boolean resultLimited;
        // For addition, if either operand is limited, then we round to the smallest scale.
        if(this.limited || other.limited) {
            resultLimited = true;
            if(this.limited && other.limited) {
                newScale = Math.min(this.scale, other.scale);
            } else if(this.limited) {
                newScale = this.scale;
            } else {
                newScale = other.scale;
            }
        } else {
            resultLimited = false;
            newScale = 0;
        }
        BigDecimal sum = this.value.add(other.value);
        if(resultLimited) {
            // Round to the newScale.
            BigDecimal rounded = sum.setScale(newScale, RoundingMode.HALF_UP);
            int newPrecision = computePrecision(rounded.toPlainString());
            return new PreciseDecimal(rounded, true, newPrecision, newScale);
        } else {
            return new PreciseDecimal(sum, false, 0, 0);
        }
    }

    /**
     * Subtracts another PreciseDecimal from this one.
     * Rounding follows the same rule as for addition.
     *
     * @param other
     *         the PreciseDecimal to subtract.
     *
     * @return a new PreciseDecimal representing the difference.
     */
    public PreciseDecimal subtract(PreciseDecimal other) {
        int newScale;
        boolean resultLimited;
        if(this.limited || other.limited) {
            resultLimited = true;
            if(this.limited && other.limited) {
                newScale = Math.min(this.scale, other.scale);
            } else if(this.limited) {
                newScale = this.scale;
            } else {
                newScale = other.scale;
            }
        } else {
            resultLimited = false;
            newScale = 0;
        }
        BigDecimal diff = this.value.subtract(other.value);
        if(resultLimited) {
            BigDecimal rounded = diff.setScale(newScale, RoundingMode.HALF_UP);
            int newPrecision = computePrecision(rounded.toPlainString());
            return new PreciseDecimal(rounded, true, newPrecision, newScale);
        } else {
            return new PreciseDecimal(diff, false, 0, 0);
        }
    }

    /**
     * Multiplies this number by another.
     * For multiplication, if either operand has limited precision, the result’s
     * significant digit count is set to the minimum of the two (treating unlimited as infinite).
     *
     * @param other
     *         the PreciseDecimal to multiply by.
     *
     * @return a new PreciseDecimal representing the product.
     */
    public PreciseDecimal multiply(PreciseDecimal other) {
        int effectivePrecision = Math.min(getEffectivePrecision(this), getEffectivePrecision(other));
        if(effectivePrecision == Integer.MAX_VALUE) {
            // Both operands are unlimited.
            BigDecimal product = this.value.multiply(other.value);
            return new PreciseDecimal(product, false, 0, product.scale());
        } else {
            MathContext mc = new MathContext(effectivePrecision, RoundingMode.HALF_UP);
            BigDecimal product = this.value.multiply(other.value, mc);
            // Use the resulting scale from BigDecimal.
            int newScale = product.scale();
            return new PreciseDecimal(product, true, effectivePrecision, newScale);
        }
    }

    /**
     * Divides this number by another.
     * If either operand is limited, the result’s precision is determined from the minimum
     * of the two; otherwise, a default scale is used for exact values.
     *
     * @param other
     *         the PreciseDecimal to divide by.
     *
     * @return a new PreciseDecimal representing the quotient.
     *
     * @throws ArithmeticException
     *         if division by zero occurs.
     */
    public PreciseDecimal divide(PreciseDecimal other) {
        if(other.value.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        int effectivePrecision = Math.min(getEffectivePrecision(this), getEffectivePrecision(other));
        if(effectivePrecision == Integer.MAX_VALUE) {
            // Both operands are unlimited; choose a default scale.

            BigDecimal quotient = this.value.divide(other.value, 20, RoundingMode.HALF_UP);
            return new PreciseDecimal(quotient, true, 20, quotient.scale());
        } else {
            MathContext mc = new MathContext(effectivePrecision, RoundingMode.HALF_UP);
            BigDecimal quotient = this.value.divide(other.value, mc);
            int newScale = quotient.scale();
            return new PreciseDecimal(quotient, true, effectivePrecision, newScale);
        }
    }

    /**
     * Raises this value to the power of the given integer exponent.
     * If the base is limited in precision, the resulting precision is estimated
     * as precision * abs(exponent). Scale is adjusted accordingly.
     *
     * @param exponent integer exponent (can be negative)
     * @return a new PreciseDecimal representing the power result
     */
    public PreciseDecimal pow(int exponent) {
        if (exponent == 0) {
            return new PreciseDecimal("1");
        }

        if (this.value.compareTo(BigDecimal.ZERO) == 0 && exponent < 0) {
            throw new ArithmeticException("0 cannot be raised to a negative power");
        }

        boolean resultLimited = this.limited;
        int resultPrecision = limited ? this.precision * Math.abs(exponent) : 0;

        // If negative exponent, use MathContext to limit precision for division
        if (exponent < 0) {
            MathContext mc = limited
                             ? new MathContext(resultPrecision, RoundingMode.HALF_UP)
                             : new MathContext(20, RoundingMode.HALF_UP); // default if unlimited

            BigDecimal powered = BigDecimal.ONE.divide(this.value.pow(-exponent, mc), mc);
            int resultScale = powered.scale();
            return new PreciseDecimal(powered, resultLimited, resultPrecision, resultScale);
        } else {
            MathContext mc = limited
                             ? new MathContext(resultPrecision, RoundingMode.HALF_UP)
                             : MathContext.UNLIMITED;

            BigDecimal powered = this.value.pow(exponent, mc);
            int resultScale = limited ? this.scale * exponent : powered.scale();
            return new PreciseDecimal(powered, resultLimited, resultPrecision, resultScale);
        }
    }

    /**
     * Returns a string representation of this number.
     * For limited values, the output is formatted to show the same number of fractional digits (scale)
     * as provided in the original input.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        if(limited) {
            // Ensure the number is displayed with the original (or resulting) scale.
            return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
        } else {
            return value.toPlainString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        PreciseDecimal that = (PreciseDecimal) o;
        return limited == that.limited && precision == that.precision && scale == that.scale && Objects.equals(
                value,
                that.value
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, limited, precision, scale);
    }

    /**
     * Helper: if a PreciseDecimal is unlimited, we treat its effective precision as infinite.
     * (For calculation purposes, we use Integer.MAX_VALUE.)
     */
    private static int getEffectivePrecision(PreciseDecimal d) {
        return d.limited ? d.precision : Integer.MAX_VALUE;
    }

    /**
     * Helper: computes a simple estimate of the number of significant digits
     * from a plain string representation.
     * This implementation removes any minus sign and decimal point, and returns the length.
     *
     * @param s
     *         the plain string (e.g. "2.00" or "0.001")
     *
     * @return the number of significant digits.
     */
    private static int computePrecision(String s) {
        s = s.trim();
        if(s.startsWith("-")) {
            s = s.substring(1);
        }
        if(s.contains(".")) {
            s = s.replace(".", "");
        }
        return s.length();
    }

    public static PreciseDecimal fromDoubleFixedScale(double value) {
        BigDecimal bd = BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
        return new PreciseDecimal(bd.toPlainString());
        //return new PreciseDecimal(bd, true, computePrecision(bd.toPlainString()), 4); // fixed sigfigs & scale
    }
}
