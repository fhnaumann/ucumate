package io.github.fhnaumann.util;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public class SyntaxVisitorHelper {

    public static UCUMExpression fromUCUMUnit(UCUMDefinition.UCUMUnit unit) {
        return switch (unit) {
            case UCUMDefinition.BaseUnit baseUnit -> new UCUMExpression.CanonicalNoPrefixSimpleUnit(baseUnit);
            case UCUMDefinition.DefinedUnit definedUnit -> new UCUMExpression.MixedNoPrefixSimpleUnit(unit);
            case null -> null;
        };
    }

    public static UCUMExpression from(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) {
        return switch (unit) {
            case UCUMDefinition.BaseUnit baseUnit -> new UCUMExpression.CanonicalPrefixSimpleUnit(prefix, baseUnit);
            case UCUMDefinition.DefinedUnit definedUnit -> new UCUMExpression.MixedPrefixSimpleUnit(prefix, unit);
            case null -> null;
        };
    }

    public static UCUMExpression from(UCUMExpression.Unit unit) {
        return switch (unit) {
            case UCUMExpression.CanonicalUnit canonicalUnit -> new UCUMExpression.CanonicalComponentNoExponent(canonicalUnit);
            case UCUMExpression.SimpleUnit simpleUnit -> new UCUMExpression.MixedComponentNoExponent(unit);
            case UCUMExpression.MixedUnit mixedUnit -> new UCUMExpression.MixedComponentNoExponent(unit);
            case null -> null;
        };
    }

    public static UCUMExpression from(UCUMExpression.Unit unit, UCUMExpression.Exponent exponent) {
        return switch (unit) {
            case UCUMExpression.CanonicalUnit canonicalUnit -> new UCUMExpression.CanonicalComponentExponent(canonicalUnit, exponent);
            case UCUMExpression.SimpleUnit simpleUnit -> new UCUMExpression.MixedComponentExponent(unit, exponent);
            case UCUMExpression.MixedUnit mixedUnit -> new UCUMExpression.MixedComponentExponent(unit, exponent);
            case null -> null;
        };
    }

    public static UCUMExpression from(UCUMExpression.Component component) {
        return switch (component) {
            case UCUMExpression.CanonicalComponent canonicalComponent -> new UCUMExpression.CanonicalComponentTerm(canonicalComponent);
            case UCUMExpression.MixedComponent mixedComponent -> new UCUMExpression.MixedComponentTerm(component);
            case null -> null;
        };
    }

    public static UCUMExpression from(UCUMExpression.Term term, UCUMExpression.Annotation annotation) {
        return switch (term) {
            case UCUMExpression.CanonicalTerm canonicalTerm -> new UCUMExpression.CanonicalAnnotTerm(canonicalTerm, annotation);
            case UCUMExpression.MixedTerm mixedTerm -> new UCUMExpression.MixedAnnotTerm(term, annotation);
            case null -> null;
        };
    }

    public static UCUMExpression fromForUnaryDiv(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.CanonicalTerm canonicalTerm -> new UCUMExpression.CanonicalUnaryDivTerm(canonicalTerm);
            case UCUMExpression.MixedTerm mixedTerm -> new UCUMExpression.MixedUnaryDivTerm(term);
            case null -> null;
        };
    }

    public static UCUMExpression from(UCUMExpression.Term left, UCUMExpression.Operator operator, UCUMExpression.Term right) {
        if(left instanceof UCUMExpression.CanonicalTerm leftCanonical && right instanceof UCUMExpression.CanonicalTerm rightCanonical) {
            return new UCUMExpression.CanonicalBinaryTerm(leftCanonical, operator, rightCanonical);
        }
        else {
            return new UCUMExpression.MixedBinaryTerm(left, operator, right);
        }
    }

    public static UCUMExpression fromForParen(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.CanonicalTerm canonicalTerm -> new UCUMExpression.CanonicalParenTerm(canonicalTerm);
            case UCUMExpression.MixedTerm mixedTerm -> new UCUMExpression.MixedParenTerm(term);
            case null -> null;
        };
    }
}
