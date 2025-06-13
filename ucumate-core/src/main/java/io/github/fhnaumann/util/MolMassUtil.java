package io.github.fhnaumann.util;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public class MolMassUtil {

    public static boolean containsMol(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> isMolUnit(componentTerm.component().unit());
            case UCUMExpression.BinaryTerm binaryTerm -> containsMol(binaryTerm.left()) || containsMol(binaryTerm.right());
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> containsMol(unaryDivTerm.term());
            case UCUMExpression.AnnotTerm annotTerm -> containsMol(annotTerm.term());
            case UCUMExpression.ParenTerm parenTerm -> containsMol(parenTerm.term());
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> false;
        };
    }

    private static boolean isMolUnit(UCUMExpression.Unit unit) {
        return switch (unit) {
            case UCUMExpression.IntegerUnit integerUnit -> false;
            case UCUMExpression.SimpleUnit simpleUnit -> simpleUnit.ucumUnit().code().equals("mol")
                    || simpleUnit.ucumUnit() instanceof UCUMDefinition.DefinedUnit definedUnit
                    && containsMol(UCUMRegistry.getInstance().getDefinedUnitSourceDefinition(definedUnit, ConfigurationRegistry.get().isEnableMolMassConversion()));
        };
    }
}
