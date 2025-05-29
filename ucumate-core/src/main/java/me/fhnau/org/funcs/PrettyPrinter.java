package me.fhnau.org.funcs;

import me.fhnau.org.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;

public class PrettyPrinter {

    private final boolean expressive;
    private final boolean fullPrefixNames;
    private final boolean fullUnitNames;
    private final boolean ucumSyntax;

    public PrettyPrinter(boolean expressive, boolean fullPrefixNames, boolean fullUnitNames, boolean ucumSyntax) {
        this.expressive = expressive;
        this.fullPrefixNames = fullPrefixNames;
        this.fullUnitNames = fullUnitNames;
        this.ucumSyntax = ucumSyntax;
    }

    public String print(UCUMExpression UCUMExpression) {
        return switch(UCUMExpression) {
            case null -> "";
            case UCUMExpression.BinaryTerm binaryTerm ->
                    print(binaryTerm.left()) + print(binaryTerm.operator()) + print(binaryTerm.right());
            case UCUMExpression.AnnotTerm annotTerm -> print(annotTerm.term()) + print(annotTerm.annotation());
            case UCUMExpression.AnnotOnlyTerm(UCUMExpression.Annotation annotation) -> print(annotation);
            case UCUMExpression.ParenTerm parenTerm -> "(%s)".formatted(print(parenTerm.term()));
            case UCUMExpression.ComponentTerm compTerm -> print(compTerm.component());
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> print(unaryDivTerm.operator()) + print(unaryDivTerm.term());
            case UCUMExpression.Annotation annotation -> "{%s}".formatted(annotation.annotation());
            case UCUMExpression.ComponentNoExponent componentNoExponent -> print(componentNoExponent.unit());
            case UCUMExpression.ComponentExponent componentExponent -> "%s^%s".formatted(print(componentExponent.unit()), print(componentExponent.exponent()));
            case UCUMExpression.IntegerUnit(int value) -> String.valueOf(value);
            case UCUMExpression.PrefixSimpleUnit prefixSimpleUnit -> printUCUMDef(prefixSimpleUnit.prefix()) + printUCUMDef(prefixSimpleUnit.ucumUnit());
            case UCUMExpression.NoPrefixSimpleUnit noPrefixSimpleUnit -> printUCUMDef(noPrefixSimpleUnit.ucumUnit());
            case UCUMExpression.Exponent exponent -> String.valueOf(exponent.exponent());
            case UCUMExpression.Operator operator -> switch(operator) {
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

    public static String defaultPrettyPrinter(UCUMExpression UCUMExpression) {
        return new PrettyPrinter(false, false, false, false).print(UCUMExpression);
    }
}
