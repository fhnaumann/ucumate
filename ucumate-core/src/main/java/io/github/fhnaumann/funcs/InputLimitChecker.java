package io.github.fhnaumann.funcs;

import io.github.fhnaumann.configuration.InputLimits;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Checks inputs against {@link InputLimits}. Each check returns the reason if a limit is exceeded and is cheap
 * compared to processing the input. The checks themselves never recurse, so they are safe for arbitrarily deep input.
 *
 * @author Felix Naumann
 */
public final class InputLimitChecker {

    // room for a sign, a decimal point and a short exponent when checking a value before parsing it
    private static final int VALUE_STRING_SLACK = 10;

    private InputLimitChecker() {
    }

    /**
     * Checks the length of a unit code before it is parsed. Text within annotations does not count towards the length.
     */
    public static Optional<String> checkUnit(String input, InputLimits limits) {
        if(!limits.enable() || input == null || input.length() <= limits.maxUnitLength()) {
            return Optional.empty();
        }
        int length = lengthWithoutAnnotationText(input);
        if(length > limits.maxUnitLength()) {
            return Optional.of("Unit is %d characters long (without annotation text), the limit is %d.".formatted(length, limits.maxUnitLength()));
        }
        return Optional.empty();
    }

    /**
     * Checks the size and the total exponent of an already parsed term.
     */
    public static Optional<String> checkTerm(UCUMExpression.Term term, InputLimits limits) {
        if(!limits.enable() || term == null) {
            return Optional.empty();
        }
        long nodes = 0;
        long totalExponent = 0;
        Deque<UCUMExpression.Term> stack = new ArrayDeque<>();
        stack.push(term);
        while(!stack.isEmpty()) {
            // every node takes up at least one character, so the node count is bounded by the unit length limit
            if(++nodes > limits.maxUnitLength()) {
                return Optional.of("Unit has more than %d terms, the limit is %d.".formatted(limits.maxUnitLength(), limits.maxUnitLength()));
            }
            switch(stack.pop()) {
                case UCUMExpression.ComponentTerm componentTerm -> totalExponent += absExponent(componentTerm.component());
                case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> {}
                case UCUMExpression.AnnotTerm annotTerm -> stack.push(annotTerm.term());
                case UCUMExpression.ParenTerm parenTerm -> stack.push(parenTerm.term());
                case UCUMExpression.UnaryDivTerm unaryDivTerm -> stack.push(unaryDivTerm.term());
                case UCUMExpression.BinaryTerm binaryTerm -> {
                    stack.push(binaryTerm.left());
                    stack.push(binaryTerm.right());
                }
            }
            if(totalExponent > limits.maxTotalExponent()) {
                return Optional.of("Unit has a total exponent of more than %d, the limit is %d.".formatted(limits.maxTotalExponent(), limits.maxTotalExponent()));
            }
        }
        return Optional.empty();
    }

    /**
     * Checks the number of digits of a numeric value.
     */
    public static Optional<String> checkValue(PreciseDecimal value, InputLimits limits) {
        if(!limits.enable() || value == null) {
            return Optional.empty();
        }
        long digits = plainDigits(value.getValue());
        if(digits > limits.maxValueDigits()) {
            return Optional.of("Value has %d digits, the limit is %d.".formatted(digits, limits.maxValueDigits()));
        }
        return Optional.empty();
    }

    /**
     * Checks a numeric value before it is parsed, because parsing a very long number is already expensive.
     */
    public static Optional<String> checkValue(String value, InputLimits limits) {
        if(!limits.enable() || value == null) {
            return Optional.empty();
        }
        long maxLength = (long) limits.maxValueDigits() + VALUE_STRING_SLACK;
        if(value.length() > maxLength) {
            return Optional.of("Value is %d characters long, the limit is %d.".formatted(value.length(), maxLength));
        }
        return Optional.empty();
    }

    private static long absExponent(UCUMExpression.Component component) {
        return switch(component) {
            case UCUMExpression.ComponentExponent componentExponent -> Math.abs((long) componentExponent.exponent().exponent());
            case UCUMExpression.ComponentNoExponent componentNoExponent -> 1;
        };
    }

    private static long plainDigits(BigDecimal value) {
        long precision = value.precision();
        long scale = value.scale();
        // negative scale: trailing zeros (5e3 -> 5000), scale larger than precision: leading zeros (1e-3 -> 0.001)
        return scale < 0 ? precision - scale : Math.max(precision, scale);
    }

    /**
     * Annotation text is a single token for the parser and does not cause any further processing cost. Only text that
     * the parser also treats as an annotation is skipped: Curly braces inside square brackets are part of the unit
     * symbol, and an annotation that is never closed is counted in full.
     */
    private static int lengthWithoutAnnotationText(String input) {
        int length = 0;
        int i = 0;
        while(i < input.length()) {
            char c = input.charAt(i);
            if(c == '[') {
                int end = input.indexOf(']', i + 1);
                end = end == -1 ? input.length() - 1 : end;
                length += end - i + 1;
                i = end + 1;
            }
            else if(c == '{') {
                int end = input.indexOf('}', i + 1);
                if(end == -1) {
                    length += input.length() - i;
                    break;
                }
                length += 2; // only count the braces
                i = end + 1;
            }
            else {
                length++;
                i++;
            }
        }
        return length;
    }
}
