package me.fhnau.org.funcs.printer;

import me.fhnau.org.model.UCUMExpression;

public class PrettyPrinter2 extends Printer {

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
