package org.example.funcs;

import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Flattener {

    public static Expression.CanonicalTerm flattenToProduct(Expression.CanonicalTerm canonicalTerm) {
        var flat = flatten(canonicalTerm);
        return buildFlatProduct(flat);
    }

    public static List<Map.Entry<Expression.CanonicalUnit, Integer>> flatten(Expression.CanonicalTerm canonicalTerm) {
        List<Map.Entry<Expression.CanonicalUnit, Integer>> list = new ArrayList<>();
        flattenImpl(canonicalTerm, 1, list);
        return list;
    }

    public static void flattenImpl(Expression.CanonicalTerm canonicalTerm, int sign, List<Map.Entry<Expression.CanonicalUnit, Integer>> out) {
        switch (canonicalTerm) {
            case Expression.CanonicalBinaryTerm binaryTerm -> {
                switch (binaryTerm.operator()) {
                    case MUL -> {
                        flattenImpl(binaryTerm.left(), sign, out);
                        flattenImpl(binaryTerm.right(), sign, out);
                    }
                    case DIV -> {
                        flattenImpl(binaryTerm.left(), sign, out);
                        flattenImpl(binaryTerm.right(), -sign, out);
                    }
                }
            }
            case Expression.CanonicalUnaryDivTerm unaryDivTerm -> flattenImpl(unaryDivTerm.term(), -sign, out);
            case Expression.CanonicalComponentTerm componentTerm -> {
                int exponent = switch (componentTerm.component()) {
                    case Expression.CanonicalComponentNoExponent componentNoExponent -> 1;
                    case Expression.CanonicalComponentExponent componentExponent -> componentExponent.exponent().exponent();
                };
                /*
                if(componentTerm.component().unit() instanceof Expression.IntegerUnit) {
                    // ignore integer units because they behave differently when cancelling out
                    return;
                }*/
                out.add(Map.entry(componentTerm.component().unit(), sign * exponent));
            }
            case Expression.CanonicalAnnotTerm canonicalAnnotTerm -> flattenImpl(canonicalAnnotTerm.term(), sign, out);
            case Expression.CanonicalParenTerm canonicalParenTerm -> flattenImpl(canonicalParenTerm.term(), sign, out);
            case Expression.AnnotOnlyTerm annotOnlyTerm -> {} // do nothing (empty map)
        }
    }

    public static Expression.CanonicalTerm buildFlatProduct(List<Map.Entry<Expression.CanonicalUnit, Integer>> map) {
        Expression.CanonicalTerm identity = (Expression.CanonicalTerm) SoloTermBuilder.UNITY;
        return map.stream()
                .reduce(
                        identity,
                        Flattener::accumulate,
                        Flattener::combineMul
                );
    }

    private static Expression.CanonicalTerm accumulate(Expression.CanonicalTerm acc, Map.Entry<Expression.CanonicalUnit, Integer> entry) {
        Expression.CanonicalUnit unit = entry.getKey();
        int exponent = entry.getValue();
        Expression.CanonicalTerm next = componentTerm(unit, exponent);
        return combineMul(acc, next);
    }

    private static Expression.CanonicalTerm combineMul(Expression.CanonicalTerm left, Expression.CanonicalTerm right) {
        return CombineTermBuilder.builder().left(left).multiplyWith().right(right).buildCanonical();
    }

    private static Expression.CanonicalTerm combineDiv(Expression.CanonicalTerm left, Expression.CanonicalTerm right) {
        return CombineTermBuilder.builder().left(left).divideBy().right(right).buildCanonical();
    }

    public static Expression.CanonicalTerm flattenAndCancel(Expression.CanonicalTerm term) {
        var flat = flatten(term);
        Map<Expression.CanonicalUnit, Integer> merged = flat.stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));
        merged.entrySet().removeIf(entry -> entry.getValue() == 0);
        return rebuildFromMap(merged);
    }

    private static Expression.CanonicalTerm rebuildFromMap(Map<Expression.CanonicalUnit, Integer> map) {
        Expression.CanonicalTerm identity = (Expression.CanonicalTerm) SoloTermBuilder.UNITY;
        Expression.CanonicalTerm num = map.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> componentTerm(entry.getKey(), entry.getValue()))
                .reduce(Flattener::combineMul)
                .orElse(identity);
        Expression.CanonicalTerm denom = map.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .map(entry -> componentTerm(entry.getKey(), -entry.getValue()))
                .reduce(Flattener::combineMul)
                .orElse(identity);
        if(denom.equals(identity)) {
            return num;
        }
        if(num.equals(identity)) {
            return CombineTermBuilder.builder().unaryDiv().right(denom).buildCanonical();
        }
        return combineDiv(num, denom);
    }

    private static Expression.CanonicalTerm componentTerm(Expression.CanonicalUnit unit, int exponent) {
        return new Expression.CanonicalComponentTerm(new Expression.CanonicalComponentExponent(unit, new Expression.Exponent(exponent)));
    }

}
