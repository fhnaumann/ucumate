package io.github.fhnaumann.funcs;

import io.github.fhnaumann.funcs.Canonicalizer.CanonicalizationResult;
import io.github.fhnaumann.funcs.Converter.ConversionResult;
import io.github.fhnaumann.funcs.printer.*;
import io.github.fhnaumann.funcs.printer.Printer.PrintType;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

import java.util.Map;

/**
 * This class provides all the functionality of the ucumate library in a centralized place.
 * This includes validation, canonicalization, conversion, commensurability, and printing.
 * Each functionality is overloaded to take either a string or an already parsed input.
 *
 * @author Felix Naumann
 */
public class UCUMService implements IUCUMService {

    private static final Map<PrintType, Printer> DEFAULT_PRINTERS = Map.of(
            PrintType.UCUM_SYNTAX, new UCUMSyntaxPrinter(),
            PrintType.EXPRESSIVE_UCUM_SYNTAX, new ExpressiveUCUMSyntaxPrinter(),
            PrintType.COMMON_MATH_SYNTAX, new WolframAlphaSyntaxPrinter(),
            PrintType.LATEX_SYNTAX, new LatexPrinter()
    );

    private CanonicalizerService canonicalizerService;
    private ConverterService converterService;
    private ValidatorService validatorService;
    private RelationCheckerService relationCheckerService;
    private final Map<PrintType, Printer> printers;

    public UCUMService() {
        this(
                null,
                null,
                new Validator(),
                null,
                DEFAULT_PRINTERS
        );
    }

    public UCUMService(CanonicalizerService canonicalizerService, ConverterService converterService, ValidatorService validatorService, RelationCheckerService relationCheckerService, Map<PrintType, Printer> printers) {
        this.canonicalizerService = canonicalizerService;
        this.converterService = converterService;
        this.validatorService = validatorService;
        this.relationCheckerService = relationCheckerService;
        this.printers = printers;
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
        return printers.get(printType).print(ucumExpression);
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
    public UCUMExpression.Term parseOrError(String input) {
        return switch (validate(input)) {
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
}
