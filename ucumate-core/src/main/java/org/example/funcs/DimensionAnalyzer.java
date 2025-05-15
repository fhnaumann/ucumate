package org.example.funcs;

import org.example.model.Expression;
import org.example.util.MapUtil;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DimensionAnalyzer {

    public sealed interface ComparisonResult {}
    record Success() implements ComparisonResult {}
    record Failure(Map<Dimension, Integer> difference) implements ComparisonResult {}


    public static ComparisonResult compare(Expression.CanonicalTerm term, Expression.CanonicalTerm otherTerm) {
        Map<Dimension, Integer> termDims = analyze(term, 1);
        Map<Dimension, Integer> otherTermDims = analyze(otherTerm, 1);
        Map<Dimension, Integer> result = MapUtil.calculateDiff(termDims, otherTermDims, true);
        if(result.isEmpty()) {
            return new Success();
        }
        else {
            return new Failure(result);
        }
    }

    public static Map<Dimension, Integer> analyze(Expression.CanonicalTerm term) {
        Map<Dimension, Integer> map = analyze(term, 1);
        //return filterEmpty(map);
        return map;
    }

    private static Map<Dimension, Integer> analyze(Expression.CanonicalTerm term, int sign) {
        return switch(term) {
            case Expression.CanonicalComponentTerm componentTerm -> analyzeComponent(componentTerm.component(), sign);
            case Expression.AnnotOnlyTerm _ -> Collections.EMPTY_MAP;
            case Expression.CanonicalAnnotTerm canonicalAnnotTerm -> analyze(canonicalAnnotTerm.term(), sign);
            case Expression.CanonicalBinaryTerm binaryTerm -> analyzeBinaryTerm(binaryTerm, sign);
            case Expression.CanonicalParenTerm canonicalParenTerm -> analyze(canonicalParenTerm.term(), sign);
            case Expression.CanonicalUnaryDivTerm canonicalUnaryDivTerm -> analyze(canonicalUnaryDivTerm.term(), -sign);
        };
    }

    private static Map<Dimension, Integer> analyzeBinaryTerm(Expression.CanonicalBinaryTerm binaryTerm, int sign) {
        Map<Dimension, Integer> leftDims = analyze(binaryTerm.left(), sign);
        int rightSign = switch(binaryTerm.operator()) {
            case MUL -> sign;
            case DIV -> -sign;
        };
        Map<Dimension, Integer> rightDims = analyze(binaryTerm.right(), rightSign);
        return Dimension.mergeDimensions(leftDims, rightDims);
    }

    private static Map<Dimension, Integer> analyzeComponent(Expression.CanonicalComponent component, int sign) {
        return switch(component) {
            case Expression.CanonicalComponentExponent(Expression.CanonicalUnit unit, Expression.Exponent(int exponent)) -> Dimension.scaleDimensions(analyzeUnit(unit, sign), sign*exponent);
            case Expression.CanonicalComponentNoExponent(Expression.CanonicalUnit unit) -> analyzeUnit(unit, sign);
        };
    }

    private static Map<Dimension, Integer> analyzeUnit(Expression.CanonicalUnit unit, int sign) {
        return switch(unit) {
            case Expression.CanonicalSimpleUnit canonicalSimpleUnit -> Map.of(Dimension.fromUCUMEssenceString(canonicalSimpleUnit.ucumUnit().dim()), sign);
            case Expression.IntegerUnit _ -> Map.of(Dimension.NO_DIMENSION, 1); // sign does not matter here (I think?)
        };
    }


}
