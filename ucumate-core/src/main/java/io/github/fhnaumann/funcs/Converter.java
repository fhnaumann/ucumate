package io.github.fhnaumann.funcs;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.Canonicalizer.UnitDirection;
import io.github.fhnaumann.funcs.DimensionAnalyzer.Failure;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.util.MolMassUtil;
import io.github.fhnaumann.util.PreciseDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Converter implements ConverterService {

    private static final Logger log = LoggerFactory.getLogger(Converter.class);

    private UcumVersion ucumVersion;
    private final PrinterService printerService;
    private final ValidatorService validatorService;

    public Converter(PrinterService printerService, ValidatorService validatorService) {
        this(ConfigurationRegistry.get().getUCUMVersionAsEnum(), printerService, validatorService);
    }

    public Converter(UcumVersion ucumVersion, PrinterService printerService, ValidatorService validatorService) {
        this.ucumVersion = ucumVersion;
        this.printerService = printerService;
        this.validatorService = validatorService;
    }

    @Override
    public UCUMExpression.Term parseOrError(String input) {
        return UCUMService.staticParseOrError(input, validatorService);
    }

    @Override
    public UcumVersion getUCUMVersion() {
        return ucumVersion;
    }

    @Override
    public void setUCUMVersion(UcumVersion ucumVersion) {
        this.ucumVersion = ucumVersion;
    }

    public record Conversion(PreciseDecimal factor, UCUMExpression.Term term) {}

    @Override
    public ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to, PreciseDecimal substanceMolMassCoeff) {
        return convert(new Conversion(factor, from), to, substanceMolMassCoeff);
    }

    public ConversionResult convert(Conversion from, UCUMExpression.Term to) {
        return convert(from, to, null);
    }

    public ConversionResult convert(Conversion from, UCUMExpression.Term to, PreciseDecimal substanceMolarMassCoeff) {
        boolean fromContainsMol = MolMassUtil.containsMol(from.term(), ucumVersion);
        boolean toContainsMol = MolMassUtil.containsMol(to, ucumVersion);
        if(ConfigurationRegistry.get().isEnableMolMassConversion() && (fromContainsMol || toContainsMol) && PreciseDecimal.ONE.equals(substanceMolarMassCoeff)) {
            log.warn("Mol <-> Mass conversion enabled and either from or to contains mol but no substanceMolarMassCoeff has been given. It is highly unlikely that a coefficient of 1 is desired.");
        }
        Canonicalizer canonicalizer = new Canonicalizer(ucumVersion, printerService, validatorService);
        CanonicalizerService.CanonicalizationResult fromResult = canonicalizer.canonicalize(from.factor(), from.term(), true, true, UnitDirection.FROM, toContainsMol ? null : substanceMolarMassCoeff);
        ScaledForm fromScaled = ScaledForm.of(fromResult);
        if(fromScaled == null) {
            return new FailedCanonicalization((CanonicalizerService.FailedCanonicalization) fromResult);
        }
        CanonicalizerService.CanonicalizationResult toResult = canonicalizer.canonicalize(fromScaled.magnitude(), to, true, true, UnitDirection.TO, fromContainsMol ? null : substanceMolarMassCoeff);
        ScaledForm toScaled = ScaledForm.of(toResult);
        if(toScaled == null) {
            return new FailedCanonicalization((CanonicalizerService.FailedCanonicalization) toResult);
        }
        /*
        Arbitrary units are part of the dimension analysis (each one is its own dimension). So if only one side contains
        arbitrary units (i.e. "[IU]" and "1"), then the dimensions don't match and this is reported as a BaseDimensionMismatch,
        the same as for two different arbitrary units (i.e. "[IU]" and "[arb'U]").
         */
        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(fromScaled.canonicalTerm(), toScaled.canonicalTerm());
        return switch (comparisonResult) {
            case Failure failure -> new BaseDimensionMismatch(failure);
            case DimensionAnalyzer.DimensionsMatch dimDimensionsMatch -> {
                if(fromScaled.hasArbitraryUnits() != toScaled.hasArbitraryUnits()) {
                    // should not be reachable because arbitrary units always introduce their own dimension, but never convert between arbitrary and non-arbitrary terms
                    yield new FailedCanonicalization((CanonicalizerService.FailedCanonicalization) (fromScaled.hasArbitraryUnits() ? fromResult : toResult));
                }
                yield new Success(toScaled.magnitude());
            }
        };
    }
}
