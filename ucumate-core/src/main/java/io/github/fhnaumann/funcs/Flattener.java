package io.github.fhnaumann.funcs;

import io.github.fhnaumann.builders.CombineTermBuilder;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Flattener {

    public static UCUMExpression.CanonicalTerm flattenToProduct(UCUMExpression.CanonicalTerm canonicalTerm) {
        var flat = flatten(canonicalTerm);
        return buildFlatProduct(flat);
    }

    public static List<Map.Entry<UCUMExpression.CanonicalUnit, Integer>> flatten(UCUMExpression.CanonicalTerm canonicalTerm) {
        List<Map.Entry<UCUMExpression.CanonicalUnit, Integer>> list = new ArrayList<>();
        flattenImpl(canonicalTerm, 1, list);
        return list;
    }

    public static void flattenImpl(UCUMExpression.CanonicalTerm canonicalTerm, int sign, List<Map.Entry<UCUMExpression.CanonicalUnit, Integer>> out) {
        switch (canonicalTerm) {
            case UCUMExpression.CanonicalBinaryTerm binaryTerm -> {
                switch (binaryTerm.operator()) {
                    case MUL -> {
                        flattenImpl(binaryTerm.left(), sign, out);
                        flattenImpl(binaryTerm.right(), sign, out);
                    }
                    case DIV -> {
                        flattenImpl(binaryTerm.left(), sign, out);
                        flattenImpl(binaryTerm.right(), -sign, out); // "-sign" or "-1*sing" ???
                    }
                }
            }
            case UCUMExpression.CanonicalUnaryDivTerm unaryDivTerm -> flattenImpl(unaryDivTerm.term(), -sign, out);
            case UCUMExpression.CanonicalComponentTerm componentTerm -> {
                int exponent = switch (componentTerm.component()) {
                    case UCUMExpression.CanonicalComponentNoExponent componentNoExponent -> 1;
                    case UCUMExpression.CanonicalComponentExponent componentExponent -> componentExponent.exponent().exponent();
                };
                /*
                if(componentTerm.component().unit() instanceof Expression.IntegerUnit) {
                    // ignore integer units because they behave differently when cancelling out
                    return;
                }*/
                out.add(Map.entry(componentTerm.component().unit(), sign * exponent));
            }
            case UCUMExpression.CanonicalAnnotTerm canonicalAnnotTerm -> flattenImpl(canonicalAnnotTerm.term(), sign, out);
            case UCUMExpression.CanonicalParenTerm canonicalParenTerm -> flattenImpl(canonicalParenTerm.term(), sign, out);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> {} // do nothing (empty map)
        }
    }

    public static UCUMExpression.CanonicalTerm buildFlatProduct(List<Map.Entry<UCUMExpression.CanonicalUnit, Integer>> map) {
        return map.stream()
                .map(entry -> componentTerm(entry.getKey(), entry.getValue())) // turn Map.Entry into CanonicalTerm
                .reduce(Flattener::combineMul)   // combine with multiplication
                .orElse((UCUMExpression.CanonicalTerm) SoloTermBuilder.UNITY); // use 1 only if truly empty
    }

    private static UCUMExpression.CanonicalTerm accumulate(UCUMExpression.CanonicalTerm acc, Map.Entry<UCUMExpression.CanonicalUnit, Integer> entry) {
        UCUMExpression.CanonicalUnit unit = entry.getKey();
        int exponent = entry.getValue();
        UCUMExpression.CanonicalTerm next = componentTerm(unit, exponent);
        return combineMul(acc, next);
    }

    private static UCUMExpression.CanonicalTerm combineMul(UCUMExpression.CanonicalTerm left, UCUMExpression.CanonicalTerm right) {
        return CombineTermBuilder.builder().left(left).multiplyWith().right(right).buildCanonical();
    }

    private static UCUMExpression.CanonicalTerm combineDiv(UCUMExpression.CanonicalTerm left, UCUMExpression.CanonicalTerm right) {
        //return CombineTermBuilder.builder().left(left).multiplyWith().right(right.invert()).buildCanonical();
        return CombineTermBuilder.builder().left(left).divideBy().right(right).buildCanonical();
    }

    public static UCUMExpression.CanonicalTerm flattenAndCancel(UCUMExpression.CanonicalTerm term) {
        var flat = flatten(term);
        Map<UCUMExpression.CanonicalUnit, Integer> merged = flat.stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));
        merged.entrySet().removeIf(entry -> entry.getValue() == 0);
        return rebuildFromMap(merged);
    }

    private static UCUMExpression.CanonicalTerm rebuildFromMap(Map<UCUMExpression.CanonicalUnit, Integer> map) {
        return map.entrySet().stream()
                .map(entry -> componentTerm(entry.getKey(), entry.getValue()))
                .reduce(Flattener::combineMul)
                .orElse((UCUMExpression.CanonicalTerm) SoloTermBuilder.UNITY);
    }

    private static UCUMExpression.CanonicalTerm componentTerm(UCUMExpression.CanonicalUnit unit, int exponent) {
        if(exponent == 1) {
            return new UCUMExpression.CanonicalComponentTerm(new UCUMExpression.CanonicalComponentNoExponent(unit));
        }
        else {
            return new UCUMExpression.CanonicalComponentTerm(new UCUMExpression.CanonicalComponentExponent(unit, new UCUMExpression.Exponent(exponent)));
        }
    }

}
