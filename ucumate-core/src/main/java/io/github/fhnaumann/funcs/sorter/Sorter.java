package io.github.fhnaumann.funcs.sorter;

import io.github.fhnaumann.builders.CombineTermBuilder;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Felix Naumann
 */
public interface Sorter {

    /**
     * Sort a flattened and normalized term.
     * The input term to be in a specific format, here's an example: "m-2.g.s-3".
     * Only base units, only multiplication, exponents (positive and negative) are allowed.
     *
     * @return A comparator for sorting.
     */
    public Comparator<UCUMExpression.CanonicalComponentTerm> sorter();



    default UCUMExpression.CanonicalTerm sort(UCUMExpression.CanonicalTerm unsortedFlattenedAndNormalizedTerm) {
        List<UCUMExpression.CanonicalComponentTerm> flattened = flatten(unsortedFlattenedAndNormalizedTerm);
        flattened.sort(sorter());
        return rebuild(flattened);
    }

    private UCUMExpression.CanonicalTerm rebuild(List<UCUMExpression.CanonicalComponentTerm> flattened) {
        return flattened.stream()
                .map(UCUMExpression.CanonicalTerm.class::cast)
                .reduce((left, right) -> CombineTermBuilder.builder().left(left).multiplyWith().right(right).buildCanonical())
                .orElse((UCUMExpression.CanonicalComponentTerm) SoloTermBuilder.UNITY);
    }

    private List<UCUMExpression.CanonicalComponentTerm> flatten(UCUMExpression.CanonicalTerm term) {
        return switch (term) {
            case UCUMExpression.CanonicalComponentTerm canonicalComponentTerm -> {
                List<UCUMExpression.CanonicalComponentTerm> list = new ArrayList<>();
                list.add(canonicalComponentTerm);
                yield list;
            }
            case UCUMExpression.CanonicalBinaryTerm canonicalBinaryTerm -> flattenBinaryTerm(canonicalBinaryTerm);
            case UCUMExpression.CanonicalAnnotTerm canonicalAnnotTerm -> flatten(canonicalAnnotTerm.term());
            case UCUMExpression.CanonicalParenTerm parenTerm -> flatten(parenTerm.term());
            case UCUMExpression.CanonicalUnaryDivTerm unaryDivTerm -> throw new RuntimeException("Cannot sort something with division.");
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> throw new RuntimeException("An annotation only term cannot be canonical.");
        };
    }

    private List<UCUMExpression.CanonicalComponentTerm> flattenBinaryTerm(UCUMExpression.CanonicalBinaryTerm canonicalBinaryTerm) {
        return switch (canonicalBinaryTerm.operator()) {
            case DIV -> throw new RuntimeException("Cannot sort something with division.");
            case MUL -> {
                List<UCUMExpression.CanonicalComponentTerm> left = flatten(canonicalBinaryTerm.left());
                List<UCUMExpression.CanonicalComponentTerm> right = flatten(canonicalBinaryTerm.right());
                List<UCUMExpression.CanonicalComponentTerm> combined = new ArrayList<>();
                combined.addAll(left);
                combined.addAll(right);
                yield combined;
            }
        };
    }
}
