package org.example.funcs;

import org.example.UCUMDefinition;
import org.example.model.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
        Map<Dimension, Integer> result = Stream.concat(termDims.keySet().stream(), otherTermDims.keySet().stream())
                     .distinct()
                     .collect(Collectors.toMap(
                             Function.identity(),
                             key -> termDims.getOrDefault(key, 0) - otherTermDims.getOrDefault(key, 0)
                     ))
                     .entrySet().stream()
                     .filter(entry -> entry.getValue() != 0)  // Remove dimensions where the difference is zero
                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if(result.isEmpty()) {
            return new Success();
        }
        else {
            return new Failure(result);
        }
    }

    public static Map<Dimension, Integer> analyze(Expression.CanonicalTerm term, int sign) {
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
        return mergeDimensions(leftDims, rightDims);
    }

    private static Map<Dimension, Integer> analyzeComponent(Expression.CanonicalComponent component, int sign) {
        return switch(component) {
            case Expression.CanonicalComponentExponent(Expression.CanonicalUnit unit, Expression.Exponent(int exponent)) -> scaleDimensions(analyzeUnit(unit), exponent);
            case Expression.CanonicalComponentNoExponent(Expression.CanonicalUnit unit) -> analyzeUnit(unit);
        };
    }

    private static Map<Dimension, Integer> scaleDimensions(Map<Dimension, Integer> dimensions, int factor) {
        return dimensions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, e -> e.getValue() * factor
                ));
    }

    private static Map<Dimension, Integer> mergeDimensions(Map<Dimension, Integer> map1, Map<Dimension, Integer> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, Integer::sum
                ));
    }

    private static Map<Dimension, Integer> analyzeUnit(Expression.CanonicalUnit unit) {
        return switch(unit) {
            case Expression.CanonicalSimpleUnit canonicalSimpleUnit -> Map.of(fromUCUMEssenceString(canonicalSimpleUnit.ucumUnit().dim()), 1);
            case Expression.IntegerUnit _ -> Map.of(Dimension.NO_DIMENSION, 1);
        };
    }

    private static Dimension fromUCUMEssenceString(String ucucmEssenceDimString) {
        return switch(ucucmEssenceDimString) {
            case "L" -> Dimension.LENGTH;
            case "T" -> Dimension.TIME;
            case "M" -> Dimension.MASS;
            case "A" -> Dimension.PLANE_ANGLE;
            case "C" -> Dimension.TEMPERATURE;
            case "Q" -> Dimension.ELECTRIC_CHARGE;
            case "F" -> Dimension.LUMINOUS_INTENSITY;
            default -> throw new IllegalArgumentException("Unknown UCUM Essence Dimension %s".formatted(ucucmEssenceDimString));
        };
    }


    public enum Dimension {
        LENGTH,
        TIME,
        MASS,
        PLANE_ANGLE,
        TEMPERATURE,
        ELECTRIC_CHARGE,
        LUMINOUS_INTENSITY,
        NO_DIMENSION;
    }

}
