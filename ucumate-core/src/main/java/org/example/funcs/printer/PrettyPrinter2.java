package org.example.funcs.printer;

import org.example.model.Expression;

public class PrettyPrinter2 extends Printer {

    @Override
    protected String printOperator(Expression.Operator operator) {
        return switch(operator) {
            case MUL -> "×";
            case DIV -> "÷";
        };
    }

    @Override
    protected String printComponentExponent(Expression.ComponentExponent componentExponent) {
        return "%s^%s".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
    }
}
