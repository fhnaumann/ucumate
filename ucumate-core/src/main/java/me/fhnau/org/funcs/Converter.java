package me.fhnau.org.funcs;

import me.fhnau.org.funcs.Canonicalizer.UnitDirection;
import me.fhnau.org.funcs.Canonicalizer.Success;
import me.fhnau.org.funcs.DimensionAnalyzer.Failure;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.util.PreciseDecimal;

public class Converter {

    public record Conversion(PreciseDecimal factor, UCUMExpression.Term term) {}

    public ConversionResult convert(UCUMExpression.Term from, UCUMExpression.Term to) {
        return convert(PreciseDecimal.ONE, from, to);
    }

    public ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to) {
        return convert(new Conversion(factor, from), to);
    }

    public ConversionResult convert(Conversion from, UCUMExpression.Term to) {
        Canonicalizer canonicalizer = new Canonicalizer();
        Canonicalizer.CanonicalizationResult fromResult = canonicalizer.canonicalize(from.factor(), from.term(), true, true, UnitDirection.FROM);
        return switch (fromResult) {
            case Canonicalizer.FailedCanonicalization failedCanonicalization -> new FailedCanonicalization(failedCanonicalization);
            case Canonicalizer.Success fromSuccess -> {
                Canonicalizer.CanonicalizationResult toResult = canonicalizer.canonicalize(fromSuccess.magnitude(), to, true, true, UnitDirection.TO);
                yield switch (toResult) {
                    case Canonicalizer.FailedCanonicalization failedCanonicalization -> new FailedCanonicalization(failedCanonicalization);
                    case Canonicalizer.Success toSuccess -> {
                        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(fromSuccess.canonicalTerm(), toSuccess.canonicalTerm());
                        yield switch (comparisonResult) {
                            case Failure failure -> new BaseDimensionMismatch(failure);
                            case DimensionAnalyzer.Success dimSuccess -> new Success(toSuccess.magnitude());
                        };
                    }
                };
            }
        };
    }
/*


    public ConversionResult convert(Conversion from, UCUMExpression.Term to) {
        Canonicalizer canonicalizer = new Canonicalizer();
        Canonicalizer.CanonicalizationResult fromResult = canonicalizer.canonicalize(from.term(), new Canonicalizer.SpecialUnitConversionContext(from.factor(), Canonicalizer.SpecialUnitApplicationDirection.FROM));
        Canonicalizer.CanonicalizationResult toResult = canonicalizer.canonicalize(to, new Canonicalizer.SpecialUnitConversionContext(from.factor(), Canonicalizer.SpecialUnitApplicationDirection.TO)); // was pd.ONE instead of "from.factor()"
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
            case DimensionAnalyzer.Success success -> {
/*
                PreciseDecimal conversionFactor;
                if(SpecialUtil.containsSpecialUnit(from.term())) {
                    // i.e. Cel->K
                    conversionFactor = PreciseDecimal.ZERO;
                }
                else {
                    // no special unit was involved during 'from' canonicalization
                    conversionFactor = from.factor().multiply(fromSuccess.conversionFactor());
                }

                if(SpecialUtil.containsSpecialUnit(to)) {
                    // i.e. K->Cel
                    conversionFactor = PreciseDecimal.ZERO;
                }
                else {
                    conversionFactor = conversionFactor.divide(toSuccess.conversionFactor());
                }
                yield new Success(conversionFactor);




                PreciseDecimal conversionFactor;
                if((toSuccess.direction() == Canonicalizer.SpecialUnitApplicationDirection.TO && SpecialUtil.containsSpecialUnit(to))) {
                    // i.e. K->Cel
                    conversionFactor = PreciseDecimal.ONE.divide(fromSuccess.conversionFactor()).multiply(toSuccess.conversionFactor());
                }
                else if((fromSuccess.direction() == Canonicalizer.SpecialUnitApplicationDirection.FROM && SpecialUtil.containsSpecialUnit(from.term()))) {
                    // i.e. Cel->K
                    conversionFactor = fromSuccess.conversionFactor().multiply(PreciseDecimal.ONE.divide(toSuccess.conversionFactor()));
                }
                else {
                    // no special unit involved
                    conversionFactor = from.factor().multiply(fromSuccess.conversionFactor()).multiply(PreciseDecimal.ONE.divide(toSuccess.conversionFactor()));

                }
                yield new Success(conversionFactor);



            }
            case DimensionAnalyzer.Failure failure -> new BaseDimensionMismatch(failure);


        };

    }

 */

    public sealed interface ConversionResult {}
    public sealed interface FailedConversion extends ConversionResult {}

    public record Success(PreciseDecimal conversionFactor) implements ConversionResult {}
    public record BaseDimensionMismatch(DimensionAnalyzer.Failure failure) implements FailedConversion {}
    public record FailedCanonicalization(Canonicalizer.FailedCanonicalization failedCanonicalization) implements FailedConversion {}
}
