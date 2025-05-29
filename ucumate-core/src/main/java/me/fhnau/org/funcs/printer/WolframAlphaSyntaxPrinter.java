package me.fhnau.org.funcs.printer;

import me.fhnau.org.model.UCUMExpression;

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
        return "(%s^%s)".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
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
        return "(" + super.printBinaryTerm(binaryTerm) + ")";
    }
}
