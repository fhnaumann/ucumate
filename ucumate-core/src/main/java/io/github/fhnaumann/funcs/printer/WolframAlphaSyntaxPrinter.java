package io.github.fhnaumann.funcs.printer;

import io.github.fhnaumann.model.UCUMExpression;

public class WolframAlphaSyntaxPrinter extends Printer {

    @Override
    protected String printOperator(UCUMExpression.Operator operator) {
        return switch (operator) {
            case MUL -> "*";
            case DIV -> "/";
        };
    }

    @Override
    protected String printComponentExponent(UCUMExpression.ComponentExponent componentExponent) {
        int exp = componentExponent.exponent().exponent();
        String unitString = print(componentExponent.unit());
        String expString = print(componentExponent.exponent());
        if(exp < 0) {
            expString = "(-%s)".formatted(expString);
        }
        String s = unitString.endsWith("^") ? "%s%s".formatted(unitString, expString) : "%s^%s".formatted(unitString, expString); // Printing the unit "10^" would otherwise lead to "10^^5"
        return s;
    }

    @Override
    protected String printBinaryTerm(UCUMExpression.BinaryTerm binaryTerm) {
        boolean leftBrackets, rightBrackets;
        if(binaryTerm.left() instanceof UCUMExpression.BinaryTerm leftBinaryTerm && leftBinaryTerm.operator() == UCUMExpression.Operator.DIV) {
            leftBrackets = true;
            //return "(%s%s%s)".formatted(print(leftBinaryTerm), print(binaryTerm.operator()), print(binaryTerm.right()));
        }
        if(binaryTerm.right() instanceof UCUMExpression.BinaryTerm rightBinaryTerm && (rightBinaryTerm.operator() == UCUMExpression.Operator.DIV || binaryTerm.operator() == UCUMExpression.Operator.DIV)) {
            rightBrackets = true;
            //return "(%s%s%s)".formatted(print(binaryTerm.left()), print(binaryTerm.operator()), print(rightBinaryTerm));
        }
        return super.printBinaryTerm(binaryTerm);
    }
}
