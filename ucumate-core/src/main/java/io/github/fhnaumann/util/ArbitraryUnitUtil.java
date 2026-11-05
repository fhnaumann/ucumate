package io.github.fhnaumann.util;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Felix Naumann
 */
public class ArbitraryUnitUtil {

    private ArbitraryUnitUtil() {}

    /**
     * Terminal arbitrary units (i.e. [iU], [arb'U]) have no definition in UCUM (the "1" in the essence is only a placeholder),
     * so they are opaque atoms in canonical terms. Non-terminal ones (i.e. [IU], defined as 1 [iU]) are aliases and get expanded.
     * @param arbitraryUnit The arbitrary unit to check.
     * @return True if the arbitrary unit is terminal, false otherwise.
     */
    public static boolean isTerminal(UCUMDefinition.ArbitraryUnit arbitraryUnit) {
        return "1".equals(arbitraryUnit.value().unit());
    }

    /**
     * Collects all arbitrary units that are present as atoms in the given canonical term.
     * @param term The canonical term to search.
     * @return An unmodifiable set (in order of appearance) of all arbitrary units in the term. Empty if there are none.
     */
    public static Set<UCUMDefinition.ArbitraryUnit> collectArbitraryUnits(UCUMExpression.CanonicalTerm term) {
        Set<UCUMDefinition.ArbitraryUnit> out = new LinkedHashSet<>();
        collectImpl(term, out);
        return Collections.unmodifiableSet(out);
    }

    private static void collectImpl(UCUMExpression.CanonicalTerm term, Set<UCUMDefinition.ArbitraryUnit> out) {
        switch (term) {
            case UCUMExpression.CanonicalComponentTerm componentTerm -> {
                if(componentTerm.component().unit() instanceof UCUMExpression.CanonicalSimpleUnit simpleUnit
                        && simpleUnit.ucumUnit() instanceof UCUMDefinition.ArbitraryUnit arbitraryUnit) {
                    out.add(arbitraryUnit);
                }
            }
            case UCUMExpression.CanonicalBinaryTerm binaryTerm -> {
                collectImpl(binaryTerm.left(), out);
                collectImpl(binaryTerm.right(), out);
            }
            case UCUMExpression.CanonicalUnaryDivTerm unaryDivTerm -> collectImpl(unaryDivTerm.term(), out);
            case UCUMExpression.CanonicalParenTerm parenTerm -> collectImpl(parenTerm.term(), out);
            case UCUMExpression.CanonicalAnnotTerm annotTerm -> collectImpl(annotTerm.term(), out);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> {} // no units
        }
    }
}
