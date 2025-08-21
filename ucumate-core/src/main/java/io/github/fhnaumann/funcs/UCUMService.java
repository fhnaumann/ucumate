package io.github.fhnaumann.funcs;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.funcs.printer.Printer.PrintType;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.util.LogUtil;
import io.github.fhnaumann.util.PreciseDecimal;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.ServiceLoader;

/**
 * This class provides all the functionality of the ucumate library in a centralized place.
 * This includes validation, canonicalization, conversion, commensurability, and printing.
 * Each functionality is overloaded to take either a string or an already parsed input.
 *
 * @author Felix Naumann
 */
public class UCUMService implements IUCUMService {

    private static final UcumVersion SELECTED_UCUM_VERSION = ConfigurationRegistry.get().getUCUMVersionAsEnum();

    private static final ValidatorService DEFAULT_VALIDATOR_SERVICE = loadService(ValidatorService.class, new Validator(SELECTED_UCUM_VERSION));
    private static final PrinterService DEFAULT_PRINTER_SERVICE = loadService(PrinterService.class, new Printer());
    private static final RelationCheckerService DEFAULT_RELATION_CHECKER_SERVICE = loadService(RelationCheckerService.class, new RelationChecker(SELECTED_UCUM_VERSION, DEFAULT_PRINTER_SERVICE, DEFAULT_VALIDATOR_SERVICE));
    private static final ConverterService DEFAULT_CONVERTER_SERVICE = loadService(ConverterService.class, new Converter(SELECTED_UCUM_VERSION, DEFAULT_PRINTER_SERVICE, DEFAULT_VALIDATOR_SERVICE));
    private static final CanonicalizerService DEFAULT_CANONICALIZER_SERVICE = loadService(CanonicalizerService.class, new Canonicalizer(SELECTED_UCUM_VERSION, DEFAULT_PRINTER_SERVICE, DEFAULT_VALIDATOR_SERVICE));
    private static final Logger log = LoggerFactory.getLogger(UCUMService.class);

    private UcumVersion ucumVersion;
    private CanonicalizerService canonicalizerService;
    private ConverterService converterService;
    private ValidatorService validatorService;
    private RelationCheckerService relationCheckerService;
    private PrinterService printerService;

    public UCUMService() {
        this(ConfigurationRegistry.get().getUCUMVersion());
    }

    public UCUMService(String ucumVersion) {
        this(UcumVersion.fromVersionString(ucumVersion));
    }

    public UCUMService(UcumVersion ucumVersion) {
        this(
                ucumVersion,
                DEFAULT_CANONICALIZER_SERVICE,
                DEFAULT_CONVERTER_SERVICE,
                DEFAULT_VALIDATOR_SERVICE,
                DEFAULT_RELATION_CHECKER_SERVICE,
                DEFAULT_PRINTER_SERVICE
        );
    }

    public UCUMService(UcumVersion ucumVersion, CanonicalizerService canonicalizerService, ConverterService converterService, ValidatorService validatorService, RelationCheckerService relationCheckerService, PrinterService printerService) {
        this.canonicalizerService = canonicalizerService;
        this.converterService = converterService;
        this.validatorService = validatorService;
        this.relationCheckerService = relationCheckerService;
        this.printerService = printerService;
        setUCUMVersion(ucumVersion);
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
    public CanonicalizationResult canonicalize(PreciseDecimal factor, UCUMExpression.Term term, PreciseDecimal substanceMolarMassCoeff) {
        return canonicalizerService.canonicalize(factor, term, substanceMolarMassCoeff);
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
    public RelationCheckerService.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2) {
        return relationCheckerService.checkRelation(term1, term2);
    }

    @Override
    public RelationCheckerService.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2) {
        return relationCheckerService.checkCommensurable(term1, term2);
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

    private void checkSameVersion(UcumVersioning versioning) {
        if(getUCUMVersion() != versioning.getUCUMVersion()) {
            LogUtil.logAndThrow(log, "Cannot use service ({}) with a different version than the composite version ({}).", versioning.getUCUMVersion(), getUCUMVersion());
        }
    }

    @Override
    public void setCanonicalizerService(CanonicalizerService canonicalizerService) {
        checkSameVersion(canonicalizerService);
        this.canonicalizerService = canonicalizerService;
    }

    @Override
    public ConverterService getConverterService() {
        return converterService;
    }

    @Override
    public void setConverterService(ConverterService converterService) {
        checkSameVersion(converterService);
        this.converterService = converterService;
    }

    @Override
    public ValidatorService getValidatorService() {
        return validatorService;
    }

    @Override
    public void setValidatorService(ValidatorService validatorService) {
        checkSameVersion(validatorService);
        this.validatorService = validatorService;
    }

    @Override
    public RelationCheckerService getRelationCheckerService() {
        return relationCheckerService;
    }

    @Override
    public void setRelationCheckerService(RelationCheckerService relationCheckerService) {
        checkSameVersion(relationCheckerService);
        this.relationCheckerService = relationCheckerService;
    }

    @Override
    public UcumVersion getUCUMVersion() {
        return ucumVersion;
    }

    @Override
    public void setUCUMVersion(UcumVersion ucumVersion) {
        validatorService.setUCUMVersion(ucumVersion);
        relationCheckerService.setUCUMVersion(ucumVersion);
        canonicalizerService.setUCUMVersion(ucumVersion);
        converterService.setUCUMVersion(ucumVersion);
        Properties old = ConfigurationRegistry.get().asProps();
        old.put("ucumate.ucumVersion", ucumVersion.getVersion());
        ConfigurationRegistry.initialize(Configuration.fromProps(old));
        this.ucumVersion = ucumVersion;
    }

    public PrinterService getPrinterService() {
        return printerService;
    }

    public void setPrinterService(PrinterService printerService) {
        this.printerService = printerService;
    }

    public VersionSpecificUCUMRegistry getUCUMRegistry() {
        return UCUMRegistry.getInstance().getVersionSpecificUCUMRegistry(getUCUMVersion());
    }
}
