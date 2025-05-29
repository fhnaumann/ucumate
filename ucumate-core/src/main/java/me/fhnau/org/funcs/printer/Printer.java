package me.fhnau.org.funcs.printer;

import me.fhnau.org.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;

/**
 * Provides methods to print the UCUMExpression model (and its subclasses) to a string representation.
 * <br>
 * You can extend this class to create your own printer implementation.
 */
public abstract class Printer {

    /**
     * The different print types.
     */
    public enum PrintType {
        /**
         * Produces valid UCUM syntax strings.
         */
        UCUM_SYNTAX,
        /**
         * Produces more detailed strings in the spirit of the UCUM syntax. But the strings are not valid UCUM syntax.
         */
        EXPRESSIVE_UCUM_SYNTAX,
        /**
         * Produces strings in the common math notation. Should be used when the term is used with a another tool that
         * does not use the UCUM syntax. I.e. it can be used to directly paste into WolframAlpha for term canonicalization.
         */
        WOLFRAM_ALPHA_SYNTAX
    }

    public String print(UCUMExpression UCUMExpression) {
        return switch(UCUMExpression) {
            case null -> printEmptyExpression();
            case UCUMExpression.BinaryTerm binaryTerm -> printBinaryTerm(binaryTerm);
            case UCUMExpression.AnnotTerm annotTerm -> printAnnotTerm(annotTerm);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> printAnnotOnlyTerm(annotOnlyTerm);
            case UCUMExpression.ParenTerm parenTerm -> printParenTerm(parenTerm);
            case UCUMExpression.ComponentTerm compTerm -> printComponentTerm(compTerm);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> printUnaryDivTerm(unaryDivTerm);
            case UCUMExpression.Annotation annotation -> printAnnotation(annotation);
            case UCUMExpression.ComponentNoExponent componentNoExponent -> printComponentNoExponent(componentNoExponent);
            case UCUMExpression.ComponentExponent componentExponent -> printComponentExponent(componentExponent);
            case UCUMExpression.IntegerUnit integerUnit -> printIntegerUnit(integerUnit);
            case UCUMExpression.PrefixSimpleUnit prefixSimpleUnit -> printPrefixSimpleUnit(prefixSimpleUnit);
            case UCUMExpression.NoPrefixSimpleUnit noPrefixSimpleUnit -> printNoPrefixSimpleUnit(noPrefixSimpleUnit);
            case UCUMExpression.Exponent exponent -> printExponent(exponent);
            case UCUMExpression.Operator operator -> printOperator(operator);
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

    protected String printOperator(UCUMExpression.Operator operator) {
        return switch(operator) {
            case MUL -> ".";
            case DIV -> "/";
        };
    }

    protected String printExponent(UCUMExpression.Exponent exponent) {
        return String.valueOf(exponent.exponent());
    }

    protected String printPrefixSimpleUnit(UCUMExpression.PrefixSimpleUnit prefixSimpleUnit) {
        return printUCUMDef(prefixSimpleUnit.prefix()) + printUCUMDef(prefixSimpleUnit.ucumUnit());
    }

    protected String printNoPrefixSimpleUnit(UCUMExpression.NoPrefixSimpleUnit noPrefixSimpleUnit) {
        return printUCUMDef(noPrefixSimpleUnit.ucumUnit());
    }

    protected String printBinaryTerm(UCUMExpression.BinaryTerm binaryTerm) {
        return print(binaryTerm.left()) + print(binaryTerm.operator()) + print(binaryTerm.right());
    }

    protected String printAnnotTerm(UCUMExpression.AnnotTerm annotTerm) {
        return print(annotTerm.term()) + print(annotTerm.annotation());
    }

    protected String printAnnotOnlyTerm(UCUMExpression.AnnotOnlyTerm annotOnlyTerm) {
        return print(annotOnlyTerm.annotation());
    }

    protected String printParenTerm(UCUMExpression.ParenTerm parenTerm) {
        return "(%s)".formatted(print(parenTerm.term()));
    }

    protected String printComponentTerm(UCUMExpression.ComponentTerm componentTerm) {
        return print(componentTerm.component());
    }

    protected String printUnaryDivTerm(UCUMExpression.UnaryDivTerm unaryDivTerm) {
        return print(unaryDivTerm.operator()) + print(unaryDivTerm.term());
    }

    protected String printAnnotation(UCUMExpression.Annotation annotation) {
        return "{%s}".formatted(annotation.annotation());
    }

    protected String printComponentNoExponent(UCUMExpression.ComponentNoExponent componentNoExponent) {
        return print(componentNoExponent.unit());
    }

    protected String printComponentExponent(UCUMExpression.ComponentExponent componentExponent) {
        return "%s%s".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
    }

    protected String printIntegerUnit(UCUMExpression.IntegerUnit integerUnit) {
        return String.valueOf(integerUnit.value());
    }

}
