package me.fhnau.org.funcs.printer;

import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;

/**
 * Renders a UCUMExpression into LaTeX string representation.
 */
public class LatexPrinter extends Printer {

    private String escapeLaTeXSymbols(String s) {
        return s.replace("\\", "\\textbackslash{}")
                .replace("_", "\\_")
                .replace("%", "\\%")
                .replace("^", "˄") // U+02C4 Modifier Letter Up Arrowhead
                .replace("{", "\\{")
                .replace("}", "\\}");
    }

    @Override
    protected String printUCUMDef(UCUMDefinition ucumDefinition) {
        return escapeLaTeXSymbols(super.printUCUMDef(ucumDefinition));

    }

    @Override
    protected String printBinaryTerm(UCUMExpression.BinaryTerm binaryTerm) {
        return switch (binaryTerm.operator()) {
            case MUL -> "%s \\cdot %s".formatted(print(binaryTerm.left()), print(binaryTerm.right()));
            case DIV -> "\\frac{%s}{%s}".formatted(print(binaryTerm.left()), print(binaryTerm.right()));
        };
    }

    @Override
    protected String printOperator(UCUMExpression.Operator operator) {
        return ""; // Operator printing is handled in binary term or unarydiv term
    }

    @Override
    protected String printParenTerm(UCUMExpression.ParenTerm parenTerm) {
        return "\\left(%s\\right)".formatted(print(parenTerm.term()));
    }

    @Override
    protected String printExponent(UCUMExpression.Exponent exponent) {
        return "^{%d}".formatted(exponent.exponent());
    }

    @Override
    protected String printAnnotation(UCUMExpression.Annotation annotation) {
        return "\\{\\text{%s}\\}".formatted(escapeLaTeXSymbols(annotation.annotation()));
    }

    @Override
    protected String printPrefixSimpleUnit(UCUMExpression.PrefixSimpleUnit prefixSimpleUnit) {
        return "\\mathrm{%s%s}".formatted(
                printUCUMDef(prefixSimpleUnit.prefix()),
                printUCUMDef(prefixSimpleUnit.ucumUnit())
        );
    }

    @Override
    protected String printNoPrefixSimpleUnit(UCUMExpression.NoPrefixSimpleUnit noPrefixSimpleUnit) {
        return "\\mathrm{%s}".formatted(printUCUMDef(noPrefixSimpleUnit.ucumUnit()));
    }

    @Override
    protected String printIntegerUnit(UCUMExpression.IntegerUnit integerUnit) {
        return "\\mathrm{%d}".formatted(integerUnit.value());
    }

    @Override
    protected String printUnaryDivTerm(UCUMExpression.UnaryDivTerm unaryDivTerm) {
        return "\\frac{1}{%s}".formatted(print(unaryDivTerm.term()));
    }
}
