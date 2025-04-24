package org.example;

import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;

public class TestUtil {

    private static final UCUMRegistry ucumRegistry = UCUMRegistry.getInstance();

    public static final UCUMDefinition.UCUMPrefix giga = getUCUMPrefix("G");
    public static final UCUMDefinition.UCUMPrefix mega = getUCUMPrefix("M");
    public static final UCUMDefinition.UCUMPrefix dezi = getUCUMPrefix("d");

    public static final UCUMDefinition.UCUMUnit meter = getUCUMUnit("m");
    public static final UCUMDefinition.UCUMUnit gram = getUCUMUnit("g");
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

    public static PreciseDecimal pd(String s) {
        return new PreciseDecimal(s);
    }

    public static Expression.Term meter_term() {
        return from(meter);
    }

    public static Expression.Term meter_parens_term() {
        return SoloTermBuilder.builder().withoutPrefix(meter).noExpNoAnnot().asTermWithParens().asTerm().build();
    }

    public static Expression.Term inch_term() {
        return from(inches);
    }

    private static Expression.Term from(UCUMDefinition.UCUMUnit ucumUnit) {
        return SoloTermBuilder.builder().withoutPrefix(ucumUnit).noExpNoAnnot().asTerm().build();
    }
}
