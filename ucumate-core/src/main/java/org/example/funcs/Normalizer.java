package org.example.funcs;

import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;

public class Normalizer {

    // TODO: Do actual normalization, not just unity replacing

    public Normalizer() {

    }

    public Expression.Term normalize(Expression.Term term) {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> componentTerm;
            case Expression.ParenTerm parenTerm -> parenTerm;
            case Expression.AnnotTerm annotTerm -> annotTerm;
            case Expression.BinaryTerm binaryTerm -> normalizeBinaryTerm(binaryTerm);
            case Expression.AnnotOnlyTerm annotOnlyTerm -> annotOnlyTerm;
            case Expression.UnaryDivTerm unaryDivTerm -> normalizeUnaryDivTerm(unaryDivTerm);
        };
    }

    private Expression.Term normalizeUnaryDivTerm(Expression.UnaryDivTerm unaryDivTerm) {
        Expression.Term normalized = normalize(unaryDivTerm.term());
        if(isUnity(normalized)) {
            return SoloTermBuilder.UNITY; // /1 -> 1
        }
        return normalized;
    }

    private Expression.Term normalizeBinaryTerm(Expression.BinaryTerm binaryTerm) {
        Expression.Term normalizedLeft = normalize(binaryTerm.left());
        Expression.Term normalizedRight = normalize(binaryTerm.right());
        boolean isLeftUnity = isUnity(normalizedLeft);
        boolean isRightUnity = isUnity(normalizedRight);
        if(isLeftUnity && isRightUnity) {
            return SoloTermBuilder.UNITY; // 1.1 -> 1 and 1/1 -> 1
        }
        else if(isRightUnity) {
            return normalizedLeft; // a.1 -> a and a/1 -> a
        }
        else if(isLeftUnity && binaryTerm.operator() == Expression.Operator.MUL) {
            return normalizedRight; // 1.b -> b
        }
        if(binaryTerm.operator() == Expression.Operator.DIV) {
            //DimensionAnalyzer.compare(normalizedLeft, normalizedRight);
        }


        return binaryTerm; // no normalization is possible
    }



    private boolean isUnity(Expression.Term term) {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> isComponentTermUnity(componentTerm);
            case Expression.ParenTerm parenTerm -> isUnity(parenTerm.term());
            case Expression.AnnotTerm annotTerm -> isUnity(annotTerm.term());
            case Expression.BinaryTerm binaryTerm -> isBinaryTermUnity(binaryTerm);
            case Expression.UnaryDivTerm unaryDivTerm -> isUnaryDivTermUnity(unaryDivTerm);
            case Expression.AnnotOnlyTerm _ -> true; // by definition an annotation is equal to the unity
        };
    }

    private boolean isBinaryTermUnity(Expression.BinaryTerm binaryTerm) {
        /*
        1/1 -> 1
        a/1 -> a
         */
        boolean leftIsUnity = isUnity(binaryTerm.left());
        boolean rightIsUnity = isUnity(binaryTerm.right());
        if(leftIsUnity && rightIsUnity) {
            return true;
        }
        else if(!leftIsUnity && rightIsUnity) {
            return true;
        }
        return false;
    }

    private boolean isUnaryDivTermUnity(Expression.UnaryDivTerm unaryDivTerm) {
        /*
        /1 -> 1
         */
        return isUnity(unaryDivTerm.term());
    }

    private boolean isComponentTermUnity(Expression.ComponentTerm componentTerm) {
        /*
        Variable 'a' is any UCUM unit.
        True if
        a==1,
        a==1 and a has an exponent which is also 1,
        a is anything and a has an exponent which is 0
         */
        return switch(componentTerm.component()) {
            case Expression.ComponentExponent componentExponent -> isIntegerUnit1(componentExponent.unit()) && componentExponent.exponent().exponent() == 1 || componentExponent.exponent().exponent() == 0;
            case Expression.ComponentNoExponent componentNoExponent -> isIntegerUnit1(componentNoExponent.unit());
        };
    }

    private boolean isIntegerUnit1(Expression.Unit unit) {
        return unit instanceof Expression.IntegerUnit integerUnit && integerUnit.value() == 1;
    }
}
