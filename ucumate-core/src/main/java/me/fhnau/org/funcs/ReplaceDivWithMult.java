package me.fhnau.org.funcs;

import me.fhnau.org.builders.CombineTermBuilder;
import me.fhnau.org.builders.SoloTermBuilder;
import me.fhnau.org.model.UCUMExpression;

public class ReplaceDivWithMult {

    public static UCUMExpression.Term replaceDivWithMult(UCUMExpression.CanonicalTerm canonicalTerm) {
        return replaceDivWithMultImpl(canonicalTerm, 1);
    }

    private static UCUMExpression.Term replaceDivWithMultImpl(UCUMExpression.CanonicalTerm canonicalTerm, int sign) {
        return switch (canonicalTerm) {
            case UCUMExpression.CanonicalComponentTerm componentTerm -> switchExponentInComponent(componentTerm.component(), sign);
            case UCUMExpression.CanonicalBinaryTerm binaryTerm -> replaceDivWithMultInBinaryTerm(binaryTerm, sign);
            case UCUMExpression.CanonicalUnaryDivTerm unaryDivTerm -> replaceDivWithMultImpl(unaryDivTerm.term(), -sign);
            case UCUMExpression.CanonicalParenTerm parenTerm -> replaceDivWithMultImpl(parenTerm.term(), sign);
            case UCUMExpression.CanonicalAnnotTerm annotTerm -> replaceDivWithMultImpl(annotTerm.term(), sign);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> SoloTermBuilder.UNITY;
        };
    }

    private static UCUMExpression.Term switchExponentInComponent(UCUMExpression.CanonicalComponent component, int sign) {
        UCUMExpression.Exponent newExponent = new UCUMExpression.Exponent(switch (component) {
            case UCUMExpression.CanonicalComponentExponent componentExponent -> sign * componentExponent.exponent().exponent();
            case UCUMExpression.CanonicalComponentNoExponent componentNoExponent -> sign;
        });
        return new UCUMExpression.CanonicalComponentTerm(new UCUMExpression.CanonicalComponentExponent(component.unit(), newExponent));
    }

    private static UCUMExpression.Term replaceDivWithMultInBinaryTerm(UCUMExpression.CanonicalBinaryTerm binaryTerm, int sign) {
        return switch (binaryTerm.operator()) {
            case MUL -> {
                UCUMExpression.Term newLeft = replaceDivWithMultImpl(binaryTerm.left(), sign);
                UCUMExpression.Term newRight = replaceDivWithMultImpl(binaryTerm.right(), sign);
                yield CombineTermBuilder.builder().left(newLeft).multiplyWith().right(newRight).buildCanonical();
            }
            case DIV -> {
                UCUMExpression.Term newLeft = replaceDivWithMultImpl(binaryTerm.left(), sign);
                UCUMExpression.Term newRight = replaceDivWithMultImpl(binaryTerm.right(), -sign); // flip only right side
                yield CombineTermBuilder.builder().left(newLeft).multiplyWith().right(newRight).buildCanonical();
            }
        };
    }
}
