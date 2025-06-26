package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;

import java.util.Map;

/**
 * @author Felix Naumann
 */
public interface RelationCheckerService extends UcumVersioning, QuickParse {

    /**
     * Checks the relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @param allowMolMassConversion If true, allows mol->g, otherwise mol->1. If the property ucumate.enableMolMassConversion=false then this value is ignored.
     * @return A RelationResult containing information about the relation between the two terms.
     *
     */
    public RelationChecker.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean allowMolMassConversion);

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation as a string. Will be validated first.
     * @param term2 The second term in the relation. Will be validated first.
     * @param allowMolMassConversion If true, allows mol->g, otherwise mol->1. If the property ucumate.enableMolMassConversion=false then this value is ignored.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see RelationChecker.CommensurableResult
     */
    public default RelationChecker.CommensurableResult checkCommensurable(String term1, String term2, boolean allowMolMassConversion) {
        try {
            return checkCommensurable(parseOrError(term1), parseOrError(term2), allowMolMassConversion);
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
        }
    }

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @param allowMolMassConversion If true, allows mol->g, otherwise mol->1. If the property ucumate.enableMolMassConversion=false then this value is ignored.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see RelationChecker.CommensurableResult
     */
    public RelationChecker.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean allowMolMassConversion);

    /**
     * Contains information about the relation check.
     */
    sealed interface RelationResult {}

    /**
     * The relation check succeeded and did not result in a failure. The subclasses provide more details.
     */
    sealed interface Success extends RelationResult {}

    /**
     * The relation check failed. The subclasses provide more details.
     */
    sealed interface FailedRelationCheck extends RelationResult permits Validator.ParserError {}

    sealed interface FailedCommensurableCheck extends RelationChecker.CommensurableResult permits Validator.ParserError {}

    /**
     * The two given terms are not equal. Information about the commensurability is found here and in the subclasses.
     */
    sealed interface CommensurableResult extends Success {}

    /**
     * The two given terms are (semantically) equal.
     * @param strictEqual True if the two terms are exactly identical (same brackets, etc.), false otherwise.
     * @param equalAfterProcessing True if the two terms are equal in their canonical form (normalized, only multiplication and exponents), false otherwise.
     */
    record IsEqual(boolean strictEqual, boolean equalAfterProcessing) implements Success {}

    /**
     * The two terms are commensurable. This is the case if they share the same base dimensions and exponents.
     */
    record IsCommensurable() implements CommensurableResult {}

    /**
     * The two terms are not commensurable. This is the case if they don't share the same base dimensions and exponents.
     * @param diff A map containing the difference between the two terms dimensions and exponents.
     */
    record NotCommensurable(Map<Dimension, Integer> diff) implements CommensurableResult {}

    /**
     * The relation check failed. This can happen when the canonicalization failed.
     */
    record Failure() implements RelationResult {}
}
