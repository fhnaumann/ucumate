package org.example.funcs;

import org.example.UCUMDefinition;
import org.example.model.Expression;

import java.util.stream.Collectors;

public class PrettyPrinter {

    private final boolean expressive;
    private final boolean fullPrefixNames;
    private final boolean fullUnitNames;

    public PrettyPrinter(boolean expressive, boolean fullPrefixNames, boolean fullUnitNames) {
        this.expressive = expressive;
        this.fullPrefixNames = fullPrefixNames;
        this.fullUnitNames = fullUnitNames;
    }

    public String print(Expression expression) {
        return switch(expression) {
            case null -> "";
            case Expression.BinaryTerm binaryTerm ->
                    print(binaryTerm.left()) + print(binaryTerm.operator()) + print(binaryTerm.right());
            case Expression.AnnotTerm annotTerm -> print(annotTerm.term()) + print(annotTerm.annotation());
            case Expression.AnnotOnlyTerm(Expression.Annotation annotation) -> print(annotation);
            case Expression.ParenTerm parenTerm -> "(%s)".formatted(print(parenTerm.term()));
            case Expression.ComponentTerm compTerm -> print(compTerm.component());
            case Expression.UnaryDivTerm unaryDivTerm -> print(unaryDivTerm.operator()) + print(unaryDivTerm.term());
            case Expression.Annotation annotation -> "{%s}".formatted(annotation.annotation());
            case Expression.ComponentNoExponent componentNoExponent -> print(componentNoExponent.unit());
            case Expression.ComponentExponent componentExponent -> "%s^%s".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
            case Expression.IntegerUnit(int value) -> String.valueOf(value);
            case Expression.PrefixSimpleUnit prefixSimpleUnit -> printUCUMDef(prefixSimpleUnit.prefix()) + printUCUMDef(prefixSimpleUnit.ucumUnit());
            case Expression.NoPrefixSimpleUnit noPrefixSimpleUnit -> printUCUMDef(noPrefixSimpleUnit.ucumUnit());
            case Expression.Exponent exponent -> String.valueOf(exponent.exponent());
            case Expression.Operator operator -> switch(operator) {
                case MUL -> "×";
                case DIV -> "÷";
            };
        };
    }

    private String printUCUMDefExpressive(UCUMDefinition ucumDefinition) {
        return switch(ucumDefinition) {
            case UCUMDefinition.Concept concept -> concept.names().size() == 1
                                                   ? concept.names().stream().findFirst().orElseThrow()
                                                   : String.join(",", concept.names());
        };
    }

    private String printUCUMDef(UCUMDefinition ucumDefinition) {
        if(expressive) {
            return printUCUMDefExpressive(ucumDefinition);
        } else {
            return switch(ucumDefinition) {
                case UCUMDefinition.Concept concept -> concept.printSymbol();
            };
        }
    }

    public static String defaultPrettyPrinter(Expression expression) {
        return new PrettyPrinter(false, false, false).print(expression);
    }
}
