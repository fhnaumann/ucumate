package org.example.funcs;

import org.example.UCUMDefinition;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;

public class Converter {

    public record Conversion(PreciseDecimal factor, Expression.Term term) {}

    public ConversionResult convert(Conversion from, Expression.Term to) {
        Canonicalizer canonicalizer = new Canonicalizer();
        Canonicalizer.CanonicalizationResult fromResult = canonicalizer.canonicalize(from.term(), new Canonicalizer.SpecialUnitConversionContext(from.factor(), Canonicalizer.SpecialUnitApplicationDirection.FROM));
        Canonicalizer.CanonicalizationResult toResult = canonicalizer.canonicalize(to, new Canonicalizer.SpecialUnitConversionContext(PreciseDecimal.ONE, Canonicalizer.SpecialUnitApplicationDirection.TO));
        if(fromResult instanceof Canonicalizer.FailedCanonicalization fromFailed) {
            return new FailedCanonicalization(fromFailed);
        }
        Canonicalizer.Success fromSuccess = (Canonicalizer.Success) fromResult;
        if(toResult instanceof Canonicalizer.FailedCanonicalization toFailed) {
            return new FailedCanonicalization(toFailed);
        }
        Canonicalizer.Success toSuccess = (Canonicalizer.Success) toResult;

        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(fromSuccess.canonicalTerm(), toSuccess.canonicalTerm());
        return switch(comparisonResult) {
            case DimensionAnalyzer.Success _ -> {
                PreciseDecimal conversionFactor;
                if((toSuccess.direction() == Canonicalizer.SpecialUnitApplicationDirection.TO && containsSpecialUnit(to))) {
                    // i.e. K->Cel
                    conversionFactor = PreciseDecimal.ONE.divide(fromSuccess.conversionFactor()).multiply(toSuccess.conversionFactor());
                }
                else if((fromSuccess.direction() == Canonicalizer.SpecialUnitApplicationDirection.FROM && containsSpecialUnit(from.term()))) {
                    // i.e. Cel->K
                    conversionFactor = fromSuccess.conversionFactor().multiply(PreciseDecimal.ONE.divide(toSuccess.conversionFactor()));
                }
                else {
                    // no special unit involved
                    conversionFactor = from.factor.multiply(fromSuccess.conversionFactor()).multiply(PreciseDecimal.ONE.divide(toSuccess.conversionFactor()));

                }
                yield new Success(conversionFactor);
            }
            case DimensionAnalyzer.Failure failure -> new BaseDimensionMismatch(failure);
        };
    }


    private boolean containsSpecialUnit(Expression.Term term) {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> containsSpecialUnit(componentTerm.component().unit());
            case Expression.AnnotTerm annotTerm -> containsSpecialUnit(annotTerm.term());
            case Expression.ParenTerm parenTerm -> containsSpecialUnit(parenTerm.term());
            case Expression.AnnotOnlyTerm _ -> false;
            case Expression.BinaryTerm binaryTerm -> containsSpecialUnit(binaryTerm.left()) || containsSpecialUnit(
                    binaryTerm.right());
            case Expression.UnaryDivTerm unaryDivTerm -> containsSpecialUnit(unaryDivTerm.term());
        };
    }

    private boolean containsSpecialUnit(Expression.Unit unit) {
        return switch(unit) {
            case Expression.SimpleUnit simpleUnit -> simpleUnit.ucumUnit() instanceof UCUMDefinition.SpecialUnit;
            case Expression.IntegerUnit _ -> false;
        };
    }

    public sealed interface ConversionResult {}
    public sealed interface FailedConversion extends ConversionResult {}

    public record Success(PreciseDecimal conversionFactor) implements ConversionResult {}
    public record BaseDimensionMismatch(DimensionAnalyzer.Failure failure) implements FailedConversion {}
    public record FailedCanonicalization(Canonicalizer.FailedCanonicalization failedCanonicalization) implements FailedConversion {}
}
