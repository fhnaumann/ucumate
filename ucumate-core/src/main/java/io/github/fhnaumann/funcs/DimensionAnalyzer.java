package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.MapUtil;

import java.util.Collections;
import java.util.Map;

public class DimensionAnalyzer {

    public sealed interface ComparisonResult {}
    public record DimensionsMatch() implements ComparisonResult {}
    public record Failure(Map<DimensionType, Integer> difference) implements ComparisonResult {}


    public static ComparisonResult compare(UCUMExpression.CanonicalTerm term, UCUMExpression.CanonicalTerm otherTerm) {
        Map<DimensionType, Integer> termDims = analyze(term, 1);
        Map<DimensionType, Integer> otherTermDims = analyze(otherTerm, 1);
        Map<DimensionType, Integer> result = MapUtil.calculateDiff(termDims, otherTermDims, true);
        if(result.isEmpty()) {
            return new DimensionsMatch();
        }
        else {
            return new Failure(result);
        }
    }

    public static Map<DimensionType, Integer> analyze(UCUMExpression.CanonicalTerm term) {
        return analyze(term, 1);
    }

    private static Map<DimensionType, Integer> analyze(UCUMExpression.CanonicalTerm term, int sign) {
        return switch(term) {
            case UCUMExpression.CanonicalComponentTerm componentTerm -> analyzeComponent(componentTerm.component(), sign);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> Collections.emptyMap();
            case UCUMExpression.CanonicalAnnotTerm canonicalAnnotTerm -> analyze(canonicalAnnotTerm.term(), sign);
            case UCUMExpression.CanonicalBinaryTerm binaryTerm -> analyzeBinaryTerm(binaryTerm, sign);
            case UCUMExpression.CanonicalParenTerm canonicalParenTerm -> analyze(canonicalParenTerm.term(), sign);
            case UCUMExpression.CanonicalUnaryDivTerm canonicalUnaryDivTerm -> analyze(canonicalUnaryDivTerm.term(), -sign);
        };
    }

    private static Map<DimensionType, Integer> analyzeBinaryTerm(UCUMExpression.CanonicalBinaryTerm binaryTerm, int sign) {
        Map<DimensionType, Integer> leftDims = analyze(binaryTerm.left(), sign);
        int rightSign = switch(binaryTerm.operator()) {
            case MUL -> sign;
            case DIV -> -sign;
        };
        Map<DimensionType, Integer> rightDims = analyze(binaryTerm.right(), rightSign);
        return DimensionType.mergeDimensions(leftDims, rightDims);
    }

    private static Map<DimensionType, Integer> analyzeComponent(UCUMExpression.CanonicalComponent component, int sign) {
        return switch(component) {
            case UCUMExpression.CanonicalComponentExponent(UCUMExpression.CanonicalUnit unit, UCUMExpression.Exponent(int exponent)) -> DimensionType.scaleDimensions(analyzeUnit(unit, sign), sign*exponent);
            case UCUMExpression.CanonicalComponentNoExponent(UCUMExpression.CanonicalUnit unit) -> analyzeUnit(unit, sign);
        };
    }

    private static Map<DimensionType, Integer> analyzeUnit(UCUMExpression.CanonicalUnit unit, int sign) {
        return switch(unit) {
            case UCUMExpression.CanonicalSimpleUnit canonicalSimpleUnit -> Map.of(DimensionType.fromUCUMEssenceString(canonicalSimpleUnit.ucumUnit().dim()), Math.abs(sign)); // was: just 'sign'
            case UCUMExpression.IntegerUnit integerUnit -> Map.of(DimensionType.NO_DIMENSION, 1); //Map.of(Dimension.NO_DIMENSION, 1); // sign does not matter here (I think?)
        };
    }


}
