package io.github.fhnaumann.funcs;

import io.github.fhnaumann.funcs.Canonicalizer.UnitDirection;
import io.github.fhnaumann.funcs.DimensionAnalyzer.Failure;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

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

    /**
     * Contains information about the conversion.
     */
    public sealed interface ConversionResult {}

    /**
     * Represents a failed conversion. The subclasses provide more details.
     */
    public sealed interface FailedConversion extends ConversionResult {}

    /**
     * The conversion was successful.
     * @param conversionFactor The resulting conversion factor. I.e. <code>x</code> in <code>factor * from = x * to</code>.
     */
    public record Success(PreciseDecimal conversionFactor) implements ConversionResult {}

    /**
     * The conversion failed because the two terms don't share the same base dimensions.
     * <br>
     * I.e. if <code>from='m'</code> and <code>to='s'</code> then they can't be converted because they are in different dimensions.
     * @param failure More details about the dimension failure.
     */
    public record BaseDimensionMismatch(DimensionAnalyzer.Failure failure) implements FailedConversion {}

    /**
     * The conversion failed because one or both terms failed the canonicalization.
     * So far this may only occur when a term contains an arbitrary unit.
     * @param failedCanonicalization More details about the canonicalization failure.
     */
    public record FailedCanonicalization(Canonicalizer.FailedCanonicalization failedCanonicalization) implements FailedConversion {}
}
