package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression.CanonicalTerm;
import io.github.fhnaumann.model.UCUMExpression.Term;

import java.util.Map;

public class RelationChecker {

    public static RelationResult checkRelation(Term term1, Term term2, boolean allowMolMassConversion) {
        Canonicalizer.CanonicalizationResult result1 = new Canonicalizer().canonicalize(term1);
        Canonicalizer.CanonicalizationResult result2 = new Canonicalizer().canonicalize(term2);
        if(!(result1 instanceof Canonicalizer.Success success1) || !(result2 instanceof Canonicalizer.Success success2)) {
            return new Failure();
        }
        boolean strictEqual = checkEquality(term1, term2);
        boolean equalAfterProcessing = checkEquality(success1.canonicalTerm(), success2.canonicalTerm());
        if(strictEqual || equalAfterProcessing) {
            return new IsEqual(strictEqual, equalAfterProcessing);
        }
        return checkCommensurable(success1.canonicalTerm(), success2.canonicalTerm(), allowMolMassConversion);
    }

    public static CommensurableResult checkCommensurable(Term term1, Term term2, boolean allowMolMassConversion) {
        Canonicalizer.CanonicalizationResult result1 = new Canonicalizer().canonicalize(term1);
        Canonicalizer.CanonicalizationResult result2 = new Canonicalizer().canonicalize(term2);
        if(!(result1 instanceof Canonicalizer.Success success1) || !(result2 instanceof Canonicalizer.Success success2)) {
            return new NotCommensurable(Map.of());
        }
        return checkCommensurable(success1.canonicalTerm(), success2.canonicalTerm(), allowMolMassConversion);

    }

    private static CommensurableResult checkCommensurable(CanonicalTerm term1, CanonicalTerm term2, boolean allowMolMassConversion) {
        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(term1, term2);
        return switch (comparisonResult) {
            case DimensionAnalyzer.Failure failure -> new NotCommensurable(failure.difference());
            case DimensionAnalyzer.Success success -> new IsCommensurable();
        };
    }

    private static boolean checkEquality(Term term1, Term term2) {
        return term1.equals(term2);
    }

    /**
     * Contains information about the relation check.
     */
    public sealed interface RelationResult {}

    /**
     * The relation check succeeded and did not result in a failure. The subclasses provide more details.
     */
    sealed interface Success extends RelationResult {}

    /**
     * The relation check failed. The subclasses provide more details.
     */
    sealed interface FailedRelationCheck extends RelationResult permits Validator.ParserError {}

    sealed interface FailedCommensurableCheck extends CommensurableResult permits Validator.ParserError {}

    /**
     * The two given terms are (semantically) equal.
     * @param strictEqual True if the two terms are exactly identical (same brackets, etc.), false otherwise.
     * @param equalAfterProcessing True if the two terms are equal in their canonical form (normalized, only multiplication and exponents), false otherwise.
     */
    record IsEqual(boolean strictEqual, boolean equalAfterProcessing) implements Success {}
    //record IsEqual() implements Success {}

    /**
     * The two given terms are not equal. Information about the commensurability is found here and in the subclasses.
     */
    public sealed interface CommensurableResult extends Success {}

    /**
     * The two terms are commensurable. This is the case if they share the same base dimensions and exponents.
     */
    public record IsCommensurable() implements CommensurableResult {}

    /**
     * The two terms are not commensurable. This is the case if they don't share the same base dimensions and exponents.
     * @param diff A map containing the difference between the two terms dimensions and exponents.
     */
    public record NotCommensurable(Map<Dimension, Integer> diff) implements CommensurableResult {}

    /**
     * The relation check failed. This can happen when the canonicalization failed.
     */
    public record Failure() implements RelationResult {}
}
