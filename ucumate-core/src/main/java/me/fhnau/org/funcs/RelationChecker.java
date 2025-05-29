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

    public sealed interface RelationResult {}
    sealed interface Success extends RelationResult {}
    record IsEqual(boolean strictEqual, boolean equalAfterProcessing) implements Success {}
    //record IsEqual() implements Success {}
    public sealed interface CommensurableResult extends Success {}
    public record IsCommensurable() implements CommensurableResult {}
    public record NotCommensurable(Map<Dimension, Integer> diff) implements CommensurableResult {}
    public record Failure() implements RelationResult {}
}
