package io.github.fhnaumann.util;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

public class SpecialUtil {

    public static boolean containsSpecialUnit(UCUMExpression.Term term) {
        return switch(term) {
            case UCUMExpression.ComponentTerm componentTerm -> containsSpecialUnit(componentTerm.component().unit());
            case UCUMExpression.AnnotTerm annotTerm -> containsSpecialUnit(annotTerm.term());
            case UCUMExpression.ParenTerm parenTerm -> containsSpecialUnit(parenTerm.term());
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> false;
            case UCUMExpression.BinaryTerm binaryTerm -> containsSpecialUnit(binaryTerm.left()) || containsSpecialUnit(
                binaryTerm.right());
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> containsSpecialUnit(unaryDivTerm.term());
        };
    }

    private static boolean containsSpecialUnit(UCUMExpression.Unit unit) {
        return switch(unit) {
            case UCUMExpression.SimpleUnit simpleUnit -> simpleUnit.ucumUnit() instanceof UCUMDefinition.SpecialUnit;
            case UCUMExpression.IntegerUnit integerUnit -> false;
        };
    }
}
