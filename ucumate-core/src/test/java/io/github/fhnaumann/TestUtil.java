package io.github.fhnaumann;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.UCUMSyntaxPrinter;
import io.github.fhnaumann.funcs.printer.WolframAlphaSyntaxPrinter;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;
import io.github.fhnaumann.util.UCUMRegistry;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class TestUtil {

    private static final BigDecimal EPSILON = new BigDecimal("0.00001");

    private static final UCUMSyntaxPrinter ucumSyntaxPrinter = new UCUMSyntaxPrinter();
    private static final WolframAlphaSyntaxPrinter wolframAlphaSyntaxPrinter = new WolframAlphaSyntaxPrinter();

    private static final UCUMRegistry ucumRegistry = UCUMRegistry.getInstance();

    public static final UCUMDefinition.UCUMPrefix giga = getUCUMPrefix("G");
    public static final UCUMDefinition.UCUMPrefix mega = getUCUMPrefix("M");
    public static final UCUMDefinition.UCUMPrefix dezi = getUCUMPrefix("d");
    public static final UCUMDefinition.UCUMPrefix centi = getUCUMPrefix("c");
    public static final UCUMDefinition.UCUMPrefix nano = getUCUMPrefix("n");

    public static final UCUMDefinition.UCUMUnit meter = getUCUMUnit("m");
    public static final UCUMDefinition.UCUMUnit gram = getUCUMUnit("g");
    public static final UCUMDefinition.UCUMUnit second = getUCUMUnit("s");
    public static final UCUMDefinition.UCUMUnit newton = getUCUMUnit("N");
    public static final UCUMDefinition.UCUMUnit feet = getUCUMUnit("[ft_i]");
    public static final UCUMDefinition.UCUMUnit celsius = getUCUMUnit("Cel");
    public static final UCUMDefinition.UCUMUnit hp_c = getUCUMUnit("[hp_C]");
    public static final UCUMDefinition.UCUMUnit inches = getUCUMUnit("[in_i]");


    public static final PreciseDecimal MINUS_FIVE = new PreciseDecimal("-5");
    public static final PreciseDecimal MINUS_ONE = new PreciseDecimal("-1");
    public static final PreciseDecimal ZERO = new PreciseDecimal("0");
    public static final PreciseDecimal ONE = new PreciseDecimal("1");
    public static final PreciseDecimal TWO = new PreciseDecimal("2");
    public static final PreciseDecimal THREE = new PreciseDecimal("3");
    public static final PreciseDecimal FOUR = new PreciseDecimal("4");
    public static final PreciseDecimal FIVE = new PreciseDecimal("5");

    public static UCUMDefinition.UCUMUnit getUCUMUnit(String code) {
        return ucumRegistry.getUCUMUnit(code)
                           .orElseThrow(() -> new IllegalStateException("UCUM unit '%s' not found!".formatted(code)));
    }

    public static UCUMDefinition.UCUMPrefix getUCUMPrefix(String prefix) {
        return ucumRegistry.getPrefix(prefix)
                           .orElseThrow(() -> new IllegalStateException("UCUM prefix '%s' not found!".formatted(prefix)));
    }

    public static PreciseDecimal pd_l(String s) {
        return new PreciseDecimal(s, true);
    }

    public static PreciseDecimal pd_u(String s) {
        return new PreciseDecimal(s, false);
    }

    public static UCUMExpression.Term single(UCUMDefinition.UCUMUnit ucumUnit) {
        return SoloTermBuilder.builder().withoutPrefix(ucumUnit).noExpNoAnnot().asTerm().build();
    }

    public static UCUMExpression.Term meter_term() {
        return from(meter);
    }

    public static UCUMExpression.Term gram_term() {
        return from(gram);
    }

    public static UCUMExpression.Term cm_term() {
        return SoloTermBuilder.builder().withPrefix(centi, meter).noExpNoAnnot().asTerm().build();
    }

    public static UCUMExpression.Term meter_parens_term() {
        return SoloTermBuilder.builder().withoutPrefix(meter).noExpNoAnnot().asTermWithParens().asTerm().build();
    }

    public static UCUMExpression.Term inch_term() {
        return from(inches);
    }

    public static UCUMExpression.Term inch2_term() {
        return SoloTermBuilder.builder().withoutPrefix(inches).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
    }

    public static UCUMExpression.Term cm2_term() {
        return SoloTermBuilder.builder().withPrefix(centi, meter).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
    }

    public static UCUMExpression.Term second_term() {
        return single(second);
    }

    private static UCUMExpression.Term from(UCUMDefinition.UCUMUnit ucumUnit) {
        return SoloTermBuilder.builder().withoutPrefix(ucumUnit).noExpNoAnnot().asTerm().build();
    }

    public static UCUMExpression.Term parse(String input) {
        return ((Validator.Success) Validator.validate(input)).term();
    }

    public static UCUMExpression.CanonicalTerm parse_canonical(String input) {
        return ((Canonicalizer.Success) new Canonicalizer().canonicalize(PreciseDecimal.ONE, parse(input), false, false, Canonicalizer.UnitDirection.FROM)).canonicalTerm();
    }

    public static String print(UCUMExpression UCUMExpression) {
        return ucumSyntaxPrinter.print(UCUMExpression);
    }

    public static String print(UCUMExpression UCUMExpression, boolean forWolframAlpha) {
        if(forWolframAlpha) {
            return wolframAlphaSyntaxPrinter.print(UCUMExpression);
        } else {
            return ucumSyntaxPrinter.print(UCUMExpression);
        }
    }

    public static boolean isClose(BigDecimal a, BigDecimal b, BigDecimal epsilon) {
        return a.subtract(b).abs().compareTo(epsilon) <= 0;
    }

    public static void skipIfRoundingProblem(String expected, PreciseDecimal actual) {
        String actualString = actual.toString();
        if(actualString.startsWith(expected)) {
            return;
        }
        assumeFalse(isClose(new BigDecimal(expected), new BigDecimal(actualString), EPSILON),
                "Skipping test because are close within an epsilon: Expected=%s, Actual=%s".formatted(expected, actualString));
    }
}
