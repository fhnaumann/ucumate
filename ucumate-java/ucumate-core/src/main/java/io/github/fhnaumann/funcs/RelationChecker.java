package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression.CanonicalTerm;
import io.github.fhnaumann.model.UCUMExpression.Term;
import io.github.fhnaumann.model.UcumVersion;

import java.util.Map;

public class RelationChecker implements RelationCheckerService {

    private UcumVersion ucumVersion;
    private final PrinterService printerService;
    private final ValidatorService validatorService;

    public RelationChecker(UcumVersion ucumVersion, PrinterService printerService, ValidatorService validatorService) {
        this.ucumVersion = ucumVersion;
        this.printerService = printerService;
        this.validatorService = validatorService;
    }

    public RelationResult checkRelation(Term term1, Term term2) {
        CanonicalizerService.CanonicalizationResult result1 = new Canonicalizer(printerService, validatorService).canonicalize(term1);
        CanonicalizerService.CanonicalizationResult result2 = new Canonicalizer(printerService, validatorService).canonicalize(term2);
        // terms with arbitrary units have no canonical form, but their scaled form can still be compared
        ScaledForm scaled1 = ScaledForm.of(result1);
        ScaledForm scaled2 = ScaledForm.of(result2);
        if(scaled1 == null || scaled2 == null) {
            return new Failure();
        }
        boolean strictEqual = checkEquality(term1, term2);
        boolean equalAfterProcessing = checkEqualityAfterProcessing(scaled1, scaled2);
        if(strictEqual || equalAfterProcessing) {
            return new IsEqual(strictEqual, equalAfterProcessing, scaled1.canonicalTerm());
        }
        return checkCommensurable(scaled1.canonicalTerm(), scaled2.canonicalTerm());
    }

    private boolean checkEqualityAfterProcessing(ScaledForm scaled1, ScaledForm scaled2) {
        return checkEquality(scaled1.canonicalTerm(), scaled2.canonicalTerm())
                && scaled1.magnitude().equals(scaled2.magnitude());
    }

    public CommensurableResult checkCommensurable(Term term1, Term term2) {
        CanonicalizerService.CanonicalizationResult result1 = new Canonicalizer(printerService, validatorService).canonicalize(term1);
        CanonicalizerService.CanonicalizationResult result2 = new Canonicalizer(printerService, validatorService).canonicalize(term2);
        // terms with arbitrary units have no canonical form, but their scaled form can still be compared
        ScaledForm scaled1 = ScaledForm.of(result1);
        ScaledForm scaled2 = ScaledForm.of(result2);
        if(scaled1 == null || scaled2 == null) {
            return new NotCommensurable(Map.of());
        }
        return checkCommensurable(scaled1.canonicalTerm(), scaled2.canonicalTerm());

    }

    private CommensurableResult checkCommensurable(CanonicalTerm term1, CanonicalTerm term2) {
        // each arbitrary unit is its own dimension, so terms with different (or without) arbitrary units are never commensurable
        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(term1, term2);
        return switch (comparisonResult) {
            case DimensionAnalyzer.Failure failure -> new NotCommensurable(failure.difference());
            case DimensionAnalyzer.DimensionsMatch dimensionsMatch -> new IsCommensurable();
        };
    }

    private boolean checkEquality(Term term1, Term term2) {
        return term1.equals(term2);
    }

    @Override
    public Term parseOrError(String input) {
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
}
