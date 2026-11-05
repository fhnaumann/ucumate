package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

import java.util.Set;

import static io.github.fhnaumann.funcs.Validator.*;

/**
 * @author Felix Naumann
 */
public interface CanonicalizerService extends UcumVersioning, QuickParse {

    /**
     * Canonicalize a UCUMTerm that is given as a string.
     * Canonicalizing includes normalizing the term. The term is simplified as much as possible and is written with only
     * multiplication and exponents.
     * <br>
     * I.e. <code>m/s2</code> becomes <code>m1.s-2</code> or <code>m/(s.m)</code> becomes <code>s-1</code>.
     *
     * @param term A term as a string. Will be validated first
     * @return A CanonicalizationResult either containing the canonical form or an error with more details.
     *
     * @see CanonicalizationResult
     * @see UCUMService#canonicalize(UCUMExpression.Term)
     */
    public default CanonicalizationResult canonicalize(String term) {
        return canonicalize(term, (PreciseDecimal) null);
    }

    public default CanonicalizationResult canonicalize(String term, PreciseDecimal substanceMolarMassCoeff) {
        try {
            return canonicalize(parseOrError(term), substanceMolarMassCoeff);
        } catch (ParserException e) {
            return new ParserError();
        }
    }

    /**
     * Canonicalize a UCUMTerm.
     * Canonicalizing includes normalizing the term. The term is simplified as much as possible and is written with only
     * multiplication and exponents.
     * <br>
     * I.e. <code>m/s2</code> becomes <code>m1.s-2</code> or <code>m/(s.m)</code> becomes <code>s-1</code>.
     *
     * @param term A term.
     * @return A CanonicalizationResult either containing the canonical form or an error with more details.
     *
     * @see CanonicalizationResult
     * @see UCUMService#canonicalize(String)
     */
    public default CanonicalizationResult canonicalize(UCUMExpression.Term term) {
        return canonicalize(PreciseDecimal.ONE, term);
    }

    public default CanonicalizationResult canonicalize(UCUMExpression.Term term, PreciseDecimal substanceMolarMassCoeff) {
        return canonicalize(PreciseDecimal.ONE, term, substanceMolarMassCoeff);
    }

    public default CanonicalizationResult canonicalize(PreciseDecimal factor, String term) {
        try {
            return canonicalize(factor, parseOrError(term));
        } catch (ParserException e) {
            return new ParserError();
        }
    }

    public default CanonicalizationResult canonicalize(String factor, UCUMExpression.Term term) {
        return canonicalize(new PreciseDecimal(factor), term);
    }

    public default CanonicalizationResult canonicalize(String factor, String term) {
        return canonicalize(new PreciseDecimal(factor), term);
    }

    public default CanonicalizationResult canonicalize(PreciseDecimal factor, UCUMExpression.Term term) {
        return canonicalize(factor, term, null);
    }

    public CanonicalizationResult canonicalize(PreciseDecimal factor, UCUMExpression.Term term, PreciseDecimal substanceMolarMassCoeff);

    /**
     * Contains information about the canonicalization.
     */
    sealed interface CanonicalizationResult {}

    /**
     * Represents a failed canonicalization. The subclasses provide more details.
     */
    sealed interface FailedCanonicalization extends CanonicalizationResult permits TermContainsPHAndCanonicalizingToMass, TermHasArbitraryUnit, ParserError {}

    /**
     * The canonicalization was successful.
     * @param magnitude The conversion factor that was created during the canonicalization.
     * @param canonicalTerm The canonical form of the given input term.
     */
    record Success(PreciseDecimal magnitude, UCUMExpression.CanonicalTerm canonicalTerm) implements
        CanonicalizationResult {}

    /**
     * The canonicalization failed because the input term contains at least one arbitrary unit. Arbitrary units are
     * not defined in terms of the base units, so a term containing them has no proper canonical form (see UCUM §24).
     * <br>
     * Nevertheless, the scaled form of the term is provided: Every arbitrary unit is treated as an opaque atom that acts
     * as its own dimension (i.e. <code>m[IU]</code> becomes the magnitude <code>0.001</code> and the term
     * <code>[iU]</code>, because <code>[IU]</code> is defined as <code>1 [iU]</code>). This allows callers to compare
     * scalar multiples of the same arbitrary unit (i.e. <code>[IU]</code> and <code>m[IU]</code>), while different arbitrary
     * units remain incommensurable with each other and with any unit that does not contain them.
     * @param arbitraryUnits The (terminal) arbitrary units that are present in the canonical term.
     * @param magnitude The conversion factor that was created during the canonicalization.
     * @param canonicalTerm The canonical form of the given input term with the arbitrary units kept as opaque atoms.
     */
    record TermHasArbitraryUnit(Set<UCUMDefinition.ArbitraryUnit> arbitraryUnits, PreciseDecimal magnitude, UCUMExpression.CanonicalTerm canonicalTerm) implements
        FailedCanonicalization {}

    /**
     * The canonicalization failed because the input term is the special unit '[pH]' and it's trying to be converted
     * to mass and mol to mass conversion is enabled in the configuration. This is just not supported (and this conversion
     * does not make any sense either).
     */
    record TermContainsPHAndCanonicalizingToMass() implements FailedCanonicalization {}
}
