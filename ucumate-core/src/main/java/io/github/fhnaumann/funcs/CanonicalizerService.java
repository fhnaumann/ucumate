package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

/**
 * @author Felix Naumann
 */
public interface CanonicalizerService extends QuickParse {

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
     * @see Canonicalizer.CanonicalizationResult
     * @see UCUMService#canonicalize(UCUMExpression.Term)
     */
    public default Canonicalizer.CanonicalizationResult canonicalize(String term) {
        try {
            return canonicalize(parseOrError(term));
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
     * @see Canonicalizer.CanonicalizationResult
     * @see UCUMService#canonicalize(String)
     */
    public default Canonicalizer.CanonicalizationResult canonicalize(UCUMExpression.Term term) {
        return canonicalize(PreciseDecimal.ONE, term);
    }

    // todo write javadoc

    public default Canonicalizer.CanonicalizationResult canonicalize(PreciseDecimal factor, String term) {
        try {
            return canonicalize(factor, parseOrError(term));
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
        }
    }

    public default Canonicalizer.CanonicalizationResult canonicalize(String factor, UCUMExpression.Term term) {
        return canonicalize(new PreciseDecimal(factor), term);
    }

    public default Canonicalizer.CanonicalizationResult canonicalize(String factor, String term) {
        return canonicalize(new PreciseDecimal(factor), term);
    }

    public Canonicalizer.CanonicalizationResult canonicalize(PreciseDecimal factor, UCUMExpression.Term term);

    /**
     * Test if a given string term is canonical.
     *
     * @param term A term as a string.
     * @return true if canonical, false otherwise.
     *
     * @see UCUMService#isCanonical(UCUMExpression.Term)
     */
    public default boolean isCanonical(String term) {
        Canonicalizer.CanonicalizationResult canonResult = canonicalize(term);
        return switch (canonResult) {
            case Canonicalizer.FailedCanonicalization failedCanonicalization -> false;
            case Canonicalizer.Success success -> true;
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
            case UCUMExpression.MixedTerm mixedTerm -> false;
        };
    }
}
