package io.github.fhnaumann;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public class CanonicalChecker {

    public static boolean containsOnlyCanonicalExpressions(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> containsOnlyCanonicalUnit(componentTerm.component().unit());
            case UCUMExpression.BinaryTerm binaryTerm -> containsOnlyCanonicalExpressions(binaryTerm.left()) && containsOnlyCanonicalExpressions(binaryTerm.right());
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> containsOnlyCanonicalExpressions(unaryDivTerm.term());
            case UCUMExpression.AnnotTerm annotTerm -> containsOnlyCanonicalExpressions(annotTerm.term());
            case UCUMExpression.ParenTerm parenTerm -> containsOnlyCanonicalExpressions(parenTerm.term());
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> true;
        };

    }

    private static boolean containsOnlyCanonicalUnit(UCUMExpression.Unit unit) {
        return switch (unit) {
            case UCUMExpression.IntegerUnit integerUnit -> true;
            case UCUMExpression.SimpleUnit simpleUnit -> switch (simpleUnit.ucumUnit()) {
                case UCUMDefinition.BaseUnit baseUnit -> true;
                case UCUMDefinition.DimlessUnit dimlessUnit -> true;
                case UCUMDefinition.DerivedUnit derivedUnit -> false;
                case UCUMDefinition.SpecialUnit specialUnit -> false;
                case UCUMDefinition.ArbitraryUnit arbitraryUnit -> false;
            };
        };
    }
}
