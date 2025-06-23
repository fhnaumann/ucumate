package io.github.fhnaumann.funcs;

import com.google.errorprone.annotations.RestrictedApi;
import io.github.fhnaumann.funcs.printer.*;
import io.github.fhnaumann.funcs.printer.Printer.PrintType;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * This class provides all the functionality of the ucumate library in a centralized place.
 * This includes validation, canonicalization, conversion, commensurability, and printing.
 * Each functionality is overloaded to take either a string or an already parsed input.
 *
 * @author Felix Naumann
 */
public class UCUMService implements IUCUMService {

    private static final ValidatorService DEFAULT_VALIDATOR_SERVICE = loadService(ValidatorService.class, new Validator());
    private static final PrinterService DEFAULT_PRINTER_SERVICE = loadService(PrinterService.class, new Printer());
    private static final RelationCheckerService DEFAULT_RELATION_CHECKER_SERVICE = loadService(RelationCheckerService.class, new RelationChecker(DEFAULT_PRINTER_SERVICE, DEFAULT_VALIDATOR_SERVICE));
    private static final ConverterService DEFAULT_CONVERTER_SERVICE = loadService(ConverterService.class, new Converter(DEFAULT_PRINTER_SERVICE, DEFAULT_VALIDATOR_SERVICE));
    private static final CanonicalizerService DEFAULT_CANONICALIZER_SERVICE = loadService(CanonicalizerService.class, new Canonicalizer(DEFAULT_PRINTER_SERVICE, DEFAULT_VALIDATOR_SERVICE));
    private static final LookupService DEFAULT_LOOKUP_SERVICE = loadService(LookupService.class, new Lookup());

    private CanonicalizerService canonicalizerService;
    private ConverterService converterService;
    private ValidatorService validatorService;
    private RelationCheckerService relationCheckerService;
    private PrinterService printerService;
    private LookupService lookupService;

    public UCUMService() {
        this(
                DEFAULT_CANONICALIZER_SERVICE,
                DEFAULT_CONVERTER_SERVICE,
                DEFAULT_VALIDATOR_SERVICE,
                DEFAULT_RELATION_CHECKER_SERVICE,
                DEFAULT_PRINTER_SERVICE,
                DEFAULT_LOOKUP_SERVICE
        );
    }

    public UCUMService(CanonicalizerService canonicalizerService, ConverterService converterService, ValidatorService validatorService, RelationCheckerService relationCheckerService, PrinterService printerService, LookupService lookupService) {
        this.canonicalizerService = canonicalizerService;
        this.converterService = converterService;
        this.validatorService = validatorService;
        this.relationCheckerService = relationCheckerService;
        this.printerService = printerService;
        this.lookupService = lookupService;
    }

    private static <T> T loadService(Class<T> clazz, T fallback) {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        return loader.findFirst().orElse(fallback);
    }

    @Override
    public ValidationResult validate(String input) {
        return validatorService.validate(input);
    }


    @Override
    public CanonicalizationResult canonicalize(PreciseDecimal factor, UCUMExpression.Term term) {
        return canonicalizerService.canonicalize(factor, term);
    }

    @Override
    public ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to, PreciseDecimal substanceMolMassCoeff) {
        return converterService.convert(factor, from, to, substanceMolMassCoeff);
    }

    @Override
    public String print(UCUMExpression ucumExpression, PrintType printType) {
        return printerService.print(ucumExpression, printType);
    }

    @Override
    public RelationChecker.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean allowMolMassConversion) {
        return relationCheckerService.checkRelation(term1, term2, allowMolMassConversion);
    }

    @Override
    public RelationChecker.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean allowMolMassConversion) {
        return relationCheckerService.checkCommensurable(term1, term2, allowMolMassConversion);
    }

    @Override
    public LookupResult lookup(String input) {
        return lookupService.lookup(input);
    }

    @Override
    public UCUMExpression.Term parseOrError(String input) {
        return staticParseOrError(input, validatorService);
    }

    public static UCUMExpression.Term staticParseOrError(String input, ValidatorService validatorService) {
        return switch (validatorService.validate(input)) {
            case Validator.Success success -> success.term();
            case Validator.Failure failure -> throw new Validator.ParserException("Failed parsing input: %s".formatted(input));
        };
    }

    @Override
    public CanonicalizerService getCanonicalizerService() {
        return canonicalizerService;
    }

    @Override
    public void setCanonicalizerService(CanonicalizerService canonicalizerService) {
        this.canonicalizerService = canonicalizerService;
    }

    @Override
    public ConverterService getConverterService() {
        return converterService;
    }

    @Override
    public void setConverterService(ConverterService converterService) {
        this.converterService = converterService;
    }

    @Override
    public ValidatorService getValidatorService() {
        return validatorService;
    }

    @Override
    public void setValidatorService(ValidatorService validatorService) {
        this.validatorService = validatorService;
    }

    @Override
    public RelationCheckerService getRelationCheckerService() {
        return relationCheckerService;
    }

    @Override
    public void setRelationCheckerService(RelationCheckerService relationCheckerService) {
        this.relationCheckerService = relationCheckerService;
    }

    @Override
    public LookupService getLookupService() {
        return lookupService;
    }

    @Override
    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    public PrinterService getPrinterService() {
        return printerService;
    }

    public void setPrinterService(PrinterService printerService) {
        this.printerService = printerService;
    }
}
