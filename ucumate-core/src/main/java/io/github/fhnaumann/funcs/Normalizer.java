package io.github.fhnaumann.funcs;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.model.UCUMExpression;

public class Normalizer {

    public Normalizer() {

    }

    public UCUMExpression.Term normalize(UCUMExpression.Term term) {
        return switch(term) {
            case UCUMExpression.ComponentTerm componentTerm -> normalizeComponent(componentTerm);
            case UCUMExpression.ParenTerm parenTerm -> parenTerm;
            case UCUMExpression.AnnotTerm annotTerm -> annotTerm;
            case UCUMExpression.BinaryTerm binaryTerm -> normalizeBinaryTerm(binaryTerm);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> annotOnlyTerm;
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> normalizeUnaryDivTerm(unaryDivTerm);
        };
    }

    private UCUMExpression.Term normalizeComponent(UCUMExpression.ComponentTerm componentTerm) {
        return switch (componentTerm.component()) {
            case UCUMExpression.ComponentNoExponent componentNoExponent -> componentTerm;
            case UCUMExpression.ComponentExponent componentExponent -> {
                int exp = componentExponent.exponent().exponent();
                if(exp == 0) {
                    yield SoloTermBuilder.UNITY; // X^0 -> 1
                }
                if(exp == -1 && isIntegerUnit1(componentExponent.unit())) { // 1^-1 -> 1
                    yield switch (componentExponent) {
                        case UCUMExpression.MixedComponentExponent mixedComponentExponent -> new UCUMExpression.MixedComponentTerm(new UCUMExpression.MixedComponentNoExponent(componentExponent.unit()));
                        case UCUMExpression.CanonicalComponentExponent canonicalComponentExponent -> new UCUMExpression.CanonicalComponentTerm(new UCUMExpression.CanonicalComponentNoExponent(canonicalComponentExponent.unit()));
                    };
                }
                yield componentTerm; // no normalization possible
            }
        };
    }

    private UCUMExpression.Term normalizeUnaryDivTerm(UCUMExpression.UnaryDivTerm unaryDivTerm) {
        UCUMExpression.Term normalized = normalize(unaryDivTerm.term());
        if(isUnity(normalized)) {
            return SoloTermBuilder.UNITY; // /1 -> 1
        }
        return normalized;
    }

    private UCUMExpression.Term normalizeBinaryTerm(UCUMExpression.BinaryTerm binaryTerm) {
        UCUMExpression.Term normalizedLeft = normalize(binaryTerm.left());
        UCUMExpression.Term normalizedRight = normalize(binaryTerm.right());
        boolean isLeftUnity = isUnity(normalizedLeft);
        boolean isRightUnity = isUnity(normalizedRight);
        if(isLeftUnity && isRightUnity) {
            return SoloTermBuilder.UNITY; // 1.1 -> 1 and 1/1 -> 1
        }
        else if(isRightUnity) {
            return normalizedLeft; // a.1 -> a and a/1 -> a
        }
        else if(isLeftUnity && binaryTerm.operator() == UCUMExpression.Operator.MUL) {
            return normalizedRight; // 1.b -> b
        }
        if(binaryTerm.operator() == UCUMExpression.Operator.DIV) {
            //DimensionAnalyzer.compare(normalizedLeft, normalizedRight);
        }


        return binaryTerm; // no normalization is possible
    }



    private boolean isUnity(UCUMExpression.Term term) {
        return switch(term) {
            case UCUMExpression.ComponentTerm componentTerm -> isComponentTermUnity(componentTerm);
            case UCUMExpression.ParenTerm parenTerm -> isUnity(parenTerm.term());
            case UCUMExpression.AnnotTerm annotTerm -> isUnity(annotTerm.term());
            case UCUMExpression.BinaryTerm binaryTerm -> isBinaryTermUnity(binaryTerm);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> isUnaryDivTermUnity(unaryDivTerm);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> true; // by definition an annotation is equal to the unity
        };
    }

    private boolean isBinaryTermUnity(UCUMExpression.BinaryTerm binaryTerm) {
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

    private boolean isUnaryDivTermUnity(UCUMExpression.UnaryDivTerm unaryDivTerm) {
        /*
        /1 -> 1
         */
        return isUnity(unaryDivTerm.term());
    }

    private boolean isComponentTermUnity(UCUMExpression.ComponentTerm componentTerm) {
        /*
        Variable 'a' is any UCUM unit.
        True if
        a==1,
        a==1 and a has an exponent which is also 1,
        a is anything and a has an exponent which is 0
         */
        return switch(componentTerm.component()) {
            case UCUMExpression.ComponentExponent componentExponent -> isIntegerUnit1(componentExponent.unit()) && componentExponent.exponent().exponent() == 1 || componentExponent.exponent().exponent() == 0;
            case UCUMExpression.ComponentNoExponent componentNoExponent -> isIntegerUnit1(componentNoExponent.unit());
        };
    }

    private boolean isIntegerUnit1(UCUMExpression.Unit unit) {
        return unit instanceof UCUMExpression.IntegerUnit integerUnit && integerUnit.value() == 1;
    }
}
