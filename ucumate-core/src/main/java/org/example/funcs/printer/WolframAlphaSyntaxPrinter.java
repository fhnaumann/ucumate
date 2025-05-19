package org.example.funcs.printer;

import org.example.model.Expression;

public class WolframAlphaSyntaxPrinter extends Printer {

    @Override
    protected String printOperator(Expression.Operator operator) {
        return switch (operator) {
            case MUL -> "*";
            case DIV -> "/";
        };
    }

    @Override
    protected String printComponentExponent(Expression.ComponentExponent componentExponent) {
        return "(%s^%s)".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
    }

    @Override
    protected String printBinaryTerm(Expression.BinaryTerm binaryTerm) {
        boolean leftBrackets, rightBrackets;
        if(binaryTerm.left() instanceof Expression.BinaryTerm leftBinaryTerm && leftBinaryTerm.operator() == Expression.Operator.DIV) {
            leftBrackets = true;
            //return "(%s%s%s)".formatted(print(leftBinaryTerm), print(binaryTerm.operator()), print(binaryTerm.right()));
        }
        if(binaryTerm.right() instanceof Expression.BinaryTerm rightBinaryTerm && (rightBinaryTerm.operator() == Expression.Operator.DIV || binaryTerm.operator() == Expression.Operator.DIV)) {
            rightBrackets = true;
            //return "(%s%s%s)".formatted(print(binaryTerm.left()), print(binaryTerm.operator()), print(rightBinaryTerm));
        }
        return "(" + super.printBinaryTerm(binaryTerm) + ")";
    }
}
