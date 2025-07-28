package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

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
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
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
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
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
     * Test if a given string term is canonical.
     *
     * @param term A term as a string.
     * @return true if canonical, false otherwise.
     *
     * @see UCUMService#isCanonical(UCUMExpression.Term)
     */
    public default boolean isCanonical(String term) {
        CanonicalizationResult canonResult = canonicalize(term);
        return switch (canonResult) {
            case FailedCanonicalization failedCanonicalization -> false;
            case Success success -> true;
        };
    }

    /**
     * Test if a given term is canonical.
     *
     * @param term A term.
     * @return true if canonical, false otherwise.
     *
     * @see UCUMService#isCanonical(String)
     */
    public default boolean isCanonical(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.CanonicalTerm canonicalTerm -> true;
            case UCUMExpression.MixedTerm mixedTerm -> switch (term) {
                case UCUMExpression.ComponentTerm componentTerm -> isCanonical(componentTerm.component().unit());
                case UCUMExpression.ParenTerm parenTerm -> isCanonical(parenTerm.term());
                case UCUMExpression.AnnotTerm annotTerm -> isCanonical(annotTerm.term());
                case UCUMExpression.UnaryDivTerm unaryDivTerm -> isCanonical(unaryDivTerm.term());
                case UCUMExpression.BinaryTerm binaryTerm -> isCanonical(binaryTerm.left()) && isCanonical(binaryTerm.right());
                case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> true;
            };
        };
    }

    public default boolean isCanonical(UCUMExpression.Unit unit) {
        return switch (unit) {
            case UCUMExpression.CanonicalUnit canonicalUnit -> true;
            case UCUMExpression.MixedUnit mixedUnit -> false;
        };
    }

    public default boolean isCanonical(UCUMDefinition.UCUMUnit ucumUnit) {
        return switch (ucumUnit) {
            case UCUMDefinition.BaseUnit baseUnit -> true;
            case UCUMDefinition.DefinedUnit definedUnit -> false;
        };
    }

    /**
     * Contains information about the canonicalization.
     */
    sealed interface CanonicalizationResult {}

    /**
     * Represents a failed canonicalization. The subclasses provide more details.
     */
    sealed interface FailedCanonicalization extends CanonicalizationResult permits TermContainsPHAndCanonicalizingToMass, TermHasArbitraryUnit, Validator.ParserError {}

    /**
     * The canonicalization was successful.
     * @param magnitude The conversion factor that was created during the canonicalization.
     * @param canonicalTerm The canonical form of the given input term.
     */
    record Success(PreciseDecimal magnitude, UCUMExpression.CanonicalTerm canonicalTerm) implements
        CanonicalizationResult {}

    /**
     * The canonicalization failed because the input term contains an arbitrary unit. Arbitrary units cannot be
     * converted to or from anything.
     * @param arbitraryUnit The arbitrary unit that was encountered and caused the failure.
     */
    record TermHasArbitraryUnit(UCUMDefinition.ArbitraryUnit arbitraryUnit) implements
        FailedCanonicalization {}

    /**
     * The canonicalization failed because the input term is the special unit '[pH]' and it's trying to be converted
     * to mass and mol to mass conversion is enabled in the configuration. This is just not supported (and this conversion
     * does not make any sense either).
     */
    record TermContainsPHAndCanonicalizingToMass() implements FailedCanonicalization {}
}
