package io.github.fhnaumann.funcs;

import io.github.fhnaumann.funcs.ValidatorService.ParserError;
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
     * @return A RelationResult containing information about the relation between the two terms.
     *
     */
    public RelationCheckerService.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2);

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation as a string. Will be validated first.
     * @param term2 The second term in the relation. Will be validated first.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see RelationChecker.CommensurableResult
     */
    public default RelationCheckerService.CommensurableResult checkCommensurable(String term1, String term2) {
        try {
            return checkCommensurable(parseOrError(term1), parseOrError(term2));
        } catch (Validator.ParserException e) {
            return new ParserError();
        }
    }

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see RelationCheckerService.CommensurableResult
     */
    public RelationCheckerService.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2);

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
    sealed interface FailedRelationCheck extends RelationResult permits FailedCommensurableCheck, Failure, ParserError {}

    sealed interface FailedCommensurableCheck extends RelationCheckerService.CommensurableResult, FailedRelationCheck permits ParserError {}

    /**
     * The two given terms are not equal. Information about the commensurability is found here and in the subclasses.
     */
    sealed interface CommensurableResult extends Success {}

    /**
     * The two given terms are (semantically) equal.
     * @param strictEqual True if the two terms are exactly identical (same brackets, etc.), false otherwise.
     * @param equalAfterProcessing True if the two terms are equal in their canonical form (normalized, only multiplication and exponents), false otherwise.
     * @param termThatIsEqual The term which is equal. For strict equality this will be the same as the input. For equality after processing this will be
     *                        the term that both inputs can be transformed to in order to create a match.
     */
    record IsEqual(boolean strictEqual, boolean equalAfterProcessing, UCUMExpression.Term termThatIsEqual) implements Success {}

    /**
     * The two terms are commensurable. This is the case if they share the same base dimensions and exponents.
     */
    record IsCommensurable() implements CommensurableResult {}

    /**
     * The two terms are not commensurable. This is the case if they don't share the same base dimensions and exponents.
     * @param diff A map containing the difference between the two terms dimensions and exponents.
     */
    record NotCommensurable(Map<DimensionType, Integer> diff) implements CommensurableResult {}

    /**
     * The relation check failed. This can happen when the canonicalization failed.
     */
    record Failure() implements FailedRelationCheck {}
}
