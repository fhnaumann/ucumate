package io.github.fhnaumann.funcs.printer;

import io.github.fhnaumann.model.UCUMExpression;

public class PrettyPrinter extends Printer {

    @Override
    protected String printOperator(UCUMExpression.Operator operator) {
        return switch(operator) {
            case MUL -> "×";
            case DIV -> "÷";
        };
    }

    @Override
    protected String printComponentExponent(UCUMExpression.ComponentExponent componentExponent) {
        return "%s^%s".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
    }
}
