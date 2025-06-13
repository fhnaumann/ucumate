package io.github.fhnaumann;

import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Felix Naumann
 */
public class UcumJavaLegacyPrinter extends Printer {

    private static final Logger log = LoggerFactory.getLogger(UcumJavaLegacyPrinter.class);

    @Override
    protected String printComponentExponent(UCUMExpression.ComponentExponent componentExponent) {
        int exp = componentExponent.exponent().exponent();
        boolean negativeSign = exp < 0;
        String sign = negativeSign ? "-" : "";
        String unitString = print(componentExponent.unit());
        return exp == 1 ? unitString : "%s ^ %s%s".formatted(unitString, sign, Math.abs(exp));
    }

    @Override
    protected String printAnnotation(UCUMExpression.Annotation annotation) {
        /*
        Annotations are always printed as " * 1", i.e. "cm{abc}" -> (centimeter) * 1
         */
        return " * 1";
    }

    @Override
    protected String printUnaryDivTerm(UCUMExpression.UnaryDivTerm unaryDivTerm) {
        /*
        UnaryDivTerms start with a space...
         */
        return " %s %s".formatted(print(unaryDivTerm.operator()), print(unaryDivTerm.term()));
    }

    @Override
    protected String printComponentTerm(UCUMExpression.ComponentTerm componentTerm) {
        /*
        Every component term is enclosed in brackets except for integer units
         */
        return switch (componentTerm.component().unit()) {
            case UCUMExpression.IntegerUnit integerUnit -> print(componentTerm.component());
            case UCUMExpression.SimpleUnit simpleUnit -> "(%s)".formatted(print(componentTerm.component()));
        };
    }

    @Override
    protected String printParenTerm(UCUMExpression.ParenTerm parenTerm) {
        /*
        UcumJava does not preserve any parenthesis which is problematic. This can mess up the logic, for example,
        the input "m/(s.g)" is printed as "m/s.g" which is something different.
        For legacy reasons this implementation mimics this (wrong) behaviour
         */
        log.warn("Parenthesis for logic ordering were detected. UcumJava does not preserve these and therefore they will be stripped. This may influence the ordering in which operations are applied. Use a different printer (from the core module) to get accurate strings.");
        return print(parenTerm.term()); // never use brackets for logic flow
    }

    @Override
    protected String printAnnotOnlyTerm(UCUMExpression.AnnotOnlyTerm annotOnlyTerm) {
        /*
        Just an annotation is always printed as "1" (unity)
         */
        return "1";
    }

    @Override
    protected String printBinaryTerm(UCUMExpression.BinaryTerm binaryTerm) {
        return "%s %s %s".formatted(print(binaryTerm.left()), print(binaryTerm.operator()), print(binaryTerm.right()));
    }

    @Override
    protected String printPrefixSimpleUnit(UCUMExpression.PrefixSimpleUnit prefixSimpleUnit) {
        /*
        Prefixes and unit are connected without a delimiter, i.e. "cm" -> "centimeter"
         */
        return "%s%s".formatted(printUCUMDef(prefixSimpleUnit.prefix()), printUCUMDef(prefixSimpleUnit.ucumUnit()));
    }

    @Override
    protected String printOperator(UCUMExpression.Operator operator) {
        return switch (operator) {
            case MUL -> "*";
            case DIV -> "/";
        };
    }

    @Override
    protected String printUCUMDef(UCUMDefinition ucumDefinition) {
        /*
        Some units (i.e. "[lton_av]") have more than one display name. UcumJava just prints the first one.
         */
        return ((UCUMDefinition.Concept) ucumDefinition).names().stream().findFirst().orElseThrow();
    }
}
