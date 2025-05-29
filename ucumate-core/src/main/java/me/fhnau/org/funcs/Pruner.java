package me.fhnau.org.funcs;

public class Pruner {

    /*
    public sealed interface Result {}
    public record Success() implements Result {}
    public record Failure() implements Result {}

    private final Map<Dimension, Integer> diffDims;

    private Pruner(Map<Dimension, Integer> startDims, Map<Dimension, Integer> targetDims) {
        this.diffDims = MapUtil.calculateDiff(startDims, targetDims, false);
    }
    public Result prune(Expression.CanonicalTerm canonicalTerm) {
        return switch (canonicalTerm) {
            case
        }
    }

    private Result pruneComponent(Expression.CanonicalComponent canonicalComponent) {
        int pruneAmount = canPruneUnit(canonicalComponent.unit());
        if(pruneAmount >= 0) {
            // this unit cannot be pruned
            return null; // todo
        }

        return switch (canonicalComponent) {

            case Expression.CanonicalComponentNoExponent canonicalComponentNoExponent ->
        }
    }

    private int canPruneUnit(Expression.CanonicalUnit canonicalUnit) {
        return switch (canonicalUnit) {
            case Expression.CanonicalSimpleUnit canonicalSimpleUnit -> canPruneCanonicalSimpleUnit(canonicalSimpleUnit);
            // Cannot prune integer units (yet), because in order to prune them, I need more information from the DimAnalyzer.
            // With integer units its not enough to just aggregate the dim number, because an integer unit behaves differently
            // when pruned because its value (or its ^-1) has to be applied before pruning. Other units can just be "silently removed".
            case Expression.IntegerUnit integerUnit -> 0;
        };
    }

    private int canPruneCanonicalSimpleUnit(Expression.CanonicalSimpleUnit canonicalSimpleUnit) {
        Dimension dim = Dimension.fromUCUMEssenceString(canonicalSimpleUnit.ucumUnit().dim());
        return diffDims.getOrDefault(dim, 0);
    }

     */
}
