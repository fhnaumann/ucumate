package org.example.funcs.printer;

import org.example.UCUMDefinition;
import org.example.model.Expression;

public abstract class Printer {

    public String print(Expression expression) {
        return switch(expression) {
            case null -> printEmptyExpression();
            case Expression.BinaryTerm binaryTerm -> printBinaryTerm(binaryTerm);
            case Expression.AnnotTerm annotTerm -> printAnnotTerm(annotTerm);
            case Expression.AnnotOnlyTerm annotOnlyTerm -> printAnnotOnlyTerm(annotOnlyTerm);
            case Expression.ParenTerm parenTerm -> printParenTerm(parenTerm);
            case Expression.ComponentTerm compTerm -> printComponentTerm(compTerm);
            case Expression.UnaryDivTerm unaryDivTerm -> printUnaryDivTerm(unaryDivTerm);
            case Expression.Annotation annotation -> printAnnotation(annotation);
            case Expression.ComponentNoExponent componentNoExponent -> printComponentNoExponent(componentNoExponent);
            case Expression.ComponentExponent componentExponent -> printComponentExponent(componentExponent);
            case Expression.IntegerUnit integerUnit -> printIntegerUnit(integerUnit);
            case Expression.PrefixSimpleUnit prefixSimpleUnit -> printPrefixSimpleUnit(prefixSimpleUnit);
            case Expression.NoPrefixSimpleUnit noPrefixSimpleUnit -> printNoPrefixSimpleUnit(noPrefixSimpleUnit);
            case Expression.Exponent exponent -> printExponent(exponent);
            case Expression.Operator operator -> printOperator(operator);
        };
    }

    protected String printUCUMDef(UCUMDefinition ucumDefinition) {
        return switch (ucumDefinition) {
            case UCUMDefinition.Concept concept -> concept.printSymbol();
        };
    }

    protected String printEmptyExpression() {
        return "<empty>";
    }

    protected String printOperator(Expression.Operator operator) {
        return switch(operator) {
            case MUL -> ".";
            case DIV -> "/";
        };
    }

    protected String printExponent(Expression.Exponent exponent) {
        return String.valueOf(exponent.exponent());
    }

    protected String printPrefixSimpleUnit(Expression.PrefixSimpleUnit prefixSimpleUnit) {
        return printUCUMDef(prefixSimpleUnit.prefix()) + printUCUMDef(prefixSimpleUnit.ucumUnit());
    }

    protected String printNoPrefixSimpleUnit(Expression.NoPrefixSimpleUnit noPrefixSimpleUnit) {
        return printUCUMDef(noPrefixSimpleUnit.ucumUnit());
    }

    protected String printBinaryTerm(Expression.BinaryTerm binaryTerm) {
        return print(binaryTerm.left()) + print(binaryTerm.operator()) + print(binaryTerm.right());
    }

    protected String printAnnotTerm(Expression.AnnotTerm annotTerm) {
        return print(annotTerm.term()) + print(annotTerm.annotation());
    }

    protected String printAnnotOnlyTerm(Expression.AnnotOnlyTerm annotOnlyTerm) {
        return print(annotOnlyTerm.annotation());
    }

    protected String printParenTerm(Expression.ParenTerm parenTerm) {
        return "(%s)".formatted(print(parenTerm.term()));
    }

    protected String printComponentTerm(Expression.ComponentTerm componentTerm) {
        return print(componentTerm.component());
    }

    protected String printUnaryDivTerm(Expression.UnaryDivTerm unaryDivTerm) {
        return print(unaryDivTerm.operator()) + print(unaryDivTerm.term());
    }

    protected String printAnnotation(Expression.Annotation annotation) {
        return "{%s}".formatted(annotation.annotation());
    }

    protected String printComponentNoExponent(Expression.ComponentNoExponent componentNoExponent) {
        return print(componentNoExponent.unit());
    }

    protected String printComponentExponent(Expression.ComponentExponent componentExponent) {
        return "%s%s".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
    }

    protected String printIntegerUnit(Expression.IntegerUnit integerUnit) {
        return String.valueOf(integerUnit);
    }

}
