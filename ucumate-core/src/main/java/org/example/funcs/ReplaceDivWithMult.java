package org.example.funcs;

import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;

public class ReplaceDivWithMult {

    public static Expression.Term replaceDivWithMult(Expression.CanonicalTerm canonicalTerm) {
        return replaceDivWithMultImpl(canonicalTerm, 1);
    }

    private static Expression.Term replaceDivWithMultImpl(Expression.CanonicalTerm canonicalTerm, int sign) {
        return switch (canonicalTerm) {
            case Expression.CanonicalComponentTerm componentTerm -> switchExponentInComponent(componentTerm.component(), sign);
            case Expression.CanonicalBinaryTerm binaryTerm -> replaceDivWithMultInBinaryTerm(binaryTerm, sign);
            case Expression.CanonicalUnaryDivTerm unaryDivTerm -> replaceDivWithMultImpl(unaryDivTerm.term(), -sign);
            case Expression.CanonicalParenTerm parenTerm -> replaceDivWithMultImpl(parenTerm.term(), sign);
            case Expression.CanonicalAnnotTerm annotTerm -> replaceDivWithMultImpl(annotTerm.term(), sign);
            case Expression.AnnotOnlyTerm annotOnlyTerm -> SoloTermBuilder.UNITY;
        };
    }

    private static Expression.Term switchExponentInComponent(Expression.CanonicalComponent component, int sign) {
        Expression.Exponent newExponent = new Expression.Exponent(switch (component) {
            case Expression.CanonicalComponentExponent componentExponent -> sign * componentExponent.exponent().exponent();
            case Expression.CanonicalComponentNoExponent componentNoExponent -> sign;
        });
        return new Expression.CanonicalComponentTerm(new Expression.CanonicalComponentExponent(component.unit(), newExponent));
    }

    private static Expression.Term replaceDivWithMultInBinaryTerm(Expression.CanonicalBinaryTerm binaryTerm, int sign) {
        return switch (binaryTerm.operator()) {
            case MUL -> {
                Expression.Term newLeft = replaceDivWithMultImpl(binaryTerm.left(), sign);
                Expression.Term newRight = replaceDivWithMultImpl(binaryTerm.right(), sign);
                yield CombineTermBuilder.builder().left(newLeft).multiplyWith().right(newRight).buildCanonical();
            }
            case DIV -> {
                Expression.Term newLeft = replaceDivWithMultImpl(binaryTerm.left(), sign);
                Expression.Term newRight = replaceDivWithMultImpl(binaryTerm.right(), -sign); // flip only right side
                yield CombineTermBuilder.builder().left(newLeft).multiplyWith().right(newRight).buildCanonical();
            }
        };
    }
}
