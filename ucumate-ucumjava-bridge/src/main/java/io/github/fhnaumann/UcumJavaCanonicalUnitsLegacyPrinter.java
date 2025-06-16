package io.github.fhnaumann;

import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public class UcumJavaCanonicalUnitsLegacyPrinter extends Printer {

    @Override
    protected String printComponentExponent(UCUMExpression.ComponentExponent componentExponent) {
        String sign = componentExponent.exponent().exponent() < 0 ? "-" : "";
        return "%s%s%s".formatted(print(componentExponent.unit()), sign, print(componentExponent.exponent()));
    }

    @Override
    protected String printIntegerUnit(UCUMExpression.IntegerUnit integerUnit) {
        if(integerUnit.value() == 1) {
            /*
            When printing the canonical form, "1" is printed as the empty string.
             */
            return "";
        }
        return super.printIntegerUnit(integerUnit);
    }
}
