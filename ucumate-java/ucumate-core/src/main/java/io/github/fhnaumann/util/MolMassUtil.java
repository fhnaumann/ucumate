package io.github.fhnaumann.util;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;

/**
 * @author Felix Naumann
 */
public class MolMassUtil {

    private MolMassUtil() {
    }

    public static boolean containsMol(UCUMExpression.Term term, UcumVersion version) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> isMolUnit(componentTerm.component().unit(), version);
            case UCUMExpression.BinaryTerm binaryTerm -> containsMol(binaryTerm.left(), version) || containsMol(binaryTerm.right(), version);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> containsMol(unaryDivTerm.term(), version);
            case UCUMExpression.AnnotTerm annotTerm -> containsMol(annotTerm.term(), version);
            case UCUMExpression.ParenTerm parenTerm -> containsMol(parenTerm.term(), version);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> false;
        };
    }

    private static boolean isMolUnit(UCUMExpression.Unit unit, UcumVersion version) {
        return switch (unit) {
            case UCUMExpression.IntegerUnit integerUnit -> false;
            case UCUMExpression.SimpleUnit simpleUnit -> simpleUnit.ucumUnit().code().equals("mol")
                    || simpleUnit.ucumUnit() instanceof UCUMDefinition.DefinedUnit definedUnit
                    && containsMol(UCUMRegistry.getInstance().getDefinedUnitSourceDefinition(definedUnit, ConfigurationRegistry.get().isEnableMolMassConversion(), version), version);
        };
    }
}
