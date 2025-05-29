package me.fhnau.org.funcs;

import me.fhnau.org.model.UCUMExpression.CanonicalTerm;
import me.fhnau.org.model.UCUMExpression.Term;

import java.util.Map;

public class RelationChecker {

    public static RelationResult checkRelation(Term term1, Term term2) {
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
        return checkCommensurable(success1.canonicalTerm(), success2.canonicalTerm());
    }

    public static CommensurableResult checkCommensurable(Term term1, Term term2) {
        Canonicalizer.CanonicalizationResult result1 = new Canonicalizer().canonicalize(term1);
        Canonicalizer.CanonicalizationResult result2 = new Canonicalizer().canonicalize(term2);
        if(!(result1 instanceof Canonicalizer.Success success1) || !(result2 instanceof Canonicalizer.Success success2)) {
            return new NotCommensurable(Map.of()); // todo returning an empty map when in reality the canonicalization failed is probably confusing...
        }
        return checkCommensurable(success1.canonicalTerm(), success2.canonicalTerm());

    }

    private static CommensurableResult checkCommensurable(CanonicalTerm term1, CanonicalTerm term2) {
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
