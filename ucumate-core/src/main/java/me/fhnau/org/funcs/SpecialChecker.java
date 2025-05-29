package me.fhnau.org.funcs;

import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public class SpecialChecker {

    public static SpecialCheckResult checkForSpecialUnitInTerm(UCUMExpression.Term term, SpecialCheckResult result) {
        // special units may only be multiplied with scalar values
        return switch(term) {
            case UCUMExpression.ComponentTerm compTerm -> checkCompTermForSpecialUnitWithExponent(compTerm, result);
            case UCUMExpression.AnnotTerm annotTerm -> checkForSpecialUnitInTerm(annotTerm.term(), result);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> SpecialCheckResult.NO_SPECIAL_UNIT_PRESENT; // annot only terms don't have special units (only the unity 1)
            case UCUMExpression.ParenTerm parenTerm -> checkForSpecialUnitInTerm(parenTerm.term(), result);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> checkUnaryDivTermForSpecialUnit(unaryDivTerm, result);
            case UCUMExpression.BinaryTerm binaryTerm -> checkBinaryTermForSpecialUnit(binaryTerm, result);
        };
    }

    public record SpecialCheckResult(boolean containsDivision, boolean containsExponent, boolean containsSpecialUnit) {

        private static final SpecialCheckResult NO_SPECIAL_UNIT_PRESENT = new SpecialCheckResult(false, false, false);

        public boolean isValid() {
            if(!containsSpecialUnit) {
                return true;
            }
            return !containsExponent && !containsDivision;
        }
    }

    private static SpecialCheckResult checkBinaryTermForSpecialUnit(UCUMExpression.BinaryTerm binaryTerm, SpecialCheckResult result) {
        SpecialCheckResult leftSpecialCheckResult = checkForSpecialUnitInTerm(binaryTerm.left(), result);
        SpecialCheckResult rightSpecialCheckResult = checkForSpecialUnitInTerm(binaryTerm.right(), result);
        boolean eitherContainsDivision = leftSpecialCheckResult.containsDivision() || rightSpecialCheckResult.containsDivision();
        boolean eitherContainsExponent = leftSpecialCheckResult.containsExponent() || rightSpecialCheckResult.containsExponent();
        boolean eitherContainsSpecial = leftSpecialCheckResult.containsSpecialUnit() || rightSpecialCheckResult.containsSpecialUnit();
        return new SpecialCheckResult(eitherContainsDivision || binaryTerm.operator() == UCUMExpression.Operator.DIV, eitherContainsExponent, eitherContainsSpecial );
    }

    private static SpecialCheckResult checkUnaryDivTermForSpecialUnit(UCUMExpression.UnaryDivTerm unaryDivTerm, SpecialCheckResult result) {
        SpecialCheckResult specialCheckResult = checkForSpecialUnitInTerm(unaryDivTerm.term(), result);
        return new SpecialCheckResult(true, specialCheckResult.containsExponent(), specialCheckResult.containsSpecialUnit());
    }

    private static SpecialCheckResult checkCompTermForSpecialUnitWithExponent(UCUMExpression.ComponentTerm compTerm, SpecialCheckResult result) {
        UCUMExpression.Unit unit = compTerm.component().unit();
        boolean containsExponent = compTerm.component() instanceof UCUMExpression.ComponentExponent componentExponent
                && (componentExponent.exponent().exponent() != 0
                || componentExponent.exponent().exponent() != 1);
        return switch(unit) {
            case UCUMExpression.IntegerUnit integerUnit -> SpecialCheckResult.NO_SPECIAL_UNIT_PRESENT; // integer units are never special
            // if it's a special unit, make sure it has no exponent
            case UCUMExpression.SimpleUnit simpleUnit -> {
                boolean isSpecial = simpleUnit.ucumUnit() instanceof UCUMDefinition.SpecialUnit;
                yield new SpecialCheckResult(result.containsDivision(), containsExponent, result.containsSpecialUnit() || isSpecial);
            }
        };
    }
}
