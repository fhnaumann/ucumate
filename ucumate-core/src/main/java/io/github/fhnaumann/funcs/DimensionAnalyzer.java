package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.MapUtil;

import java.util.Collections;
import java.util.Map;

public class DimensionAnalyzer {

    public sealed interface ComparisonResult {}
    record Success() implements ComparisonResult {}
    record Failure(Map<Dimension, Integer> difference) implements ComparisonResult {}


    public static ComparisonResult compare(UCUMExpression.CanonicalTerm term, UCUMExpression.CanonicalTerm otherTerm) {
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

    public static Map<Dimension, Integer> analyze(UCUMExpression.CanonicalTerm term) {
        Map<Dimension, Integer> map = analyze(term, 1);
        //return filterEmpty(map);
        return map;
    }

    private static Map<Dimension, Integer> analyze(UCUMExpression.CanonicalTerm term, int sign) {
        return switch(term) {
            case UCUMExpression.CanonicalComponentTerm componentTerm -> analyzeComponent(componentTerm.component(), sign);
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> Collections.EMPTY_MAP;
            case UCUMExpression.CanonicalAnnotTerm canonicalAnnotTerm -> analyze(canonicalAnnotTerm.term(), sign);
            case UCUMExpression.CanonicalBinaryTerm binaryTerm -> analyzeBinaryTerm(binaryTerm, sign);
            case UCUMExpression.CanonicalParenTerm canonicalParenTerm -> analyze(canonicalParenTerm.term(), sign);
            case UCUMExpression.CanonicalUnaryDivTerm canonicalUnaryDivTerm -> analyze(canonicalUnaryDivTerm.term(), -sign);
        };
    }

    private static Map<Dimension, Integer> analyzeBinaryTerm(UCUMExpression.CanonicalBinaryTerm binaryTerm, int sign) {
        Map<Dimension, Integer> leftDims = analyze(binaryTerm.left(), sign);
        int rightSign = switch(binaryTerm.operator()) {
            case MUL -> sign;
            case DIV -> -sign;
        };
        Map<Dimension, Integer> rightDims = analyze(binaryTerm.right(), rightSign);
        return Dimension.mergeDimensions(leftDims, rightDims);
    }

    private static Map<Dimension, Integer> analyzeComponent(UCUMExpression.CanonicalComponent component, int sign) {
        return switch(component) {
            case UCUMExpression.CanonicalComponentExponent(UCUMExpression.CanonicalUnit unit, UCUMExpression.Exponent(int exponent)) -> Dimension.scaleDimensions(analyzeUnit(unit, sign), sign*exponent);
            case UCUMExpression.CanonicalComponentNoExponent(UCUMExpression.CanonicalUnit unit) -> analyzeUnit(unit, sign);
        };
    }

    private static Map<Dimension, Integer> analyzeUnit(UCUMExpression.CanonicalUnit unit, int sign) {
        return switch(unit) {
            case UCUMExpression.CanonicalSimpleUnit canonicalSimpleUnit -> Map.of(Dimension.fromUCUMEssenceString(canonicalSimpleUnit.ucumUnit().dim()), Math.abs(sign)); // was: just 'sign'
            case UCUMExpression.IntegerUnit integerUnit -> Map.of(); //Map.of(Dimension.NO_DIMENSION, 1); // sign does not matter here (I think?)
        };
    }


}
