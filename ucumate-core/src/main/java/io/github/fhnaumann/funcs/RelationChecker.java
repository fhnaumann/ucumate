package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression.CanonicalTerm;
import io.github.fhnaumann.model.UCUMExpression.Term;

import java.util.Map;

public class RelationChecker implements RelationCheckerService {

    private final PrinterService printerService;
    private final ValidatorService validatorService;

    public RelationChecker(PrinterService printerService, ValidatorService validatorService) {
        this.printerService = printerService;
        this.validatorService = validatorService;
    }

    public RelationResult checkRelation(Term term1, Term term2, boolean allowMolMassConversion) {
        CanonicalizerService.CanonicalizationResult result1 = new Canonicalizer(printerService, validatorService).canonicalize(term1);
        CanonicalizerService.CanonicalizationResult result2 = new Canonicalizer(printerService, validatorService).canonicalize(term2);
        if(!(result1 instanceof CanonicalizerService.Success success1) || !(result2 instanceof CanonicalizerService.Success success2)) {
            return new Failure();
        }
        boolean strictEqual = checkEquality(term1, term2);
        boolean equalAfterProcessing = checkEquality(success1.canonicalTerm(), success2.canonicalTerm());
        if(strictEqual || equalAfterProcessing) {
            return new IsEqual(strictEqual, equalAfterProcessing);
        }
        return checkCommensurable(success1.canonicalTerm(), success2.canonicalTerm(), allowMolMassConversion);
    }

    public CommensurableResult checkCommensurable(Term term1, Term term2, boolean allowMolMassConversion) {
        CanonicalizerService.CanonicalizationResult result1 = new Canonicalizer(printerService, validatorService).canonicalize(term1);
        CanonicalizerService.CanonicalizationResult result2 = new Canonicalizer(printerService, validatorService).canonicalize(term2);
        if(!(result1 instanceof CanonicalizerService.Success success1) || !(result2 instanceof CanonicalizerService.Success success2)) {
            return new NotCommensurable(Map.of());
        }
        return checkCommensurable(success1.canonicalTerm(), success2.canonicalTerm(), allowMolMassConversion);

    }

    private CommensurableResult checkCommensurable(CanonicalTerm term1, CanonicalTerm term2, boolean allowMolMassConversion) {
        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(term1, term2);
        return switch (comparisonResult) {
            case DimensionAnalyzer.Failure failure -> new NotCommensurable(failure.difference());
            case DimensionAnalyzer.Success success -> new IsCommensurable();
        };
    }

    private boolean checkEquality(Term term1, Term term2) {
        return term1.equals(term2);
    }

    @Override
    public Term parseOrError(String input) {
        return UCUMService.staticParseOrError(input, validatorService);
    }

}
