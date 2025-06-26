package io.github.fhnaumann.operations.ucum.filters;

import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ucum.InvalidInputException;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.LogUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Felix Naumann
 */
public class CanonicalFilter implements ApplyFilter {

    private static final Logger log = LoggerFactory.getLogger(CanonicalFilter.class);
    private final UCUMService service;

    public CanonicalFilter(UCUMService service) {
        this.service = service;
    }

    @Override
    public Collection<UCUMExpression.Term> apply(String expression, ValueSet.FilterOperator operator) throws InvalidInputException {
        ValidatorService.ValidationResult validationResult = service.validate(expression);
        return switch (validationResult) {
            case ValidatorService.Failure failure -> throw new InvalidInputException(String.join(",", failure.errorMessages()));
            case ValidatorService.Success success -> handleSuccess(success.term(), operator);
        };
    }

    private List<UCUMExpression.Term> handleSuccess(UCUMExpression.Term term, ValueSet.FilterOperator operator) throws InvalidInputException {
        return switch (operator) {
            case EQUAL -> applyWithEqualFilter(term);
            case IN -> applyWithInFilter(term);
            default -> List.of(); // todo: others are not supported. Silently return empty or return fail?
        };
    }

    private List<UCUMExpression.Term> applyWithInFilter(UCUMExpression.Term term) {
        // todo not yet implemented
        throw new UnsupportedOperationException();
    }

    private List<UCUMExpression.Term> applyWithEqualFilter(UCUMExpression.Term term) {
        Map<Dimension, Integer> sourceDims = analyze(term);
        Map<UCUMExpression.Term, Map<Dimension, Integer>> allOtherExpressions = getAllKnownValidTerms();
        return allOtherExpressions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(sourceDims))
                .map(Map.Entry::getKey)
                .toList();
    }

    private Map<UCUMExpression.Term, Map<Dimension, Integer>> getAllKnownValidTerms() {
        Map<UCUMExpression.Term, Map<Dimension, Integer>> allOtherExpressions = new HashMap<>();
        PersistenceRegistry.getInstance().getAllValidated().forEach((valKey, validationResult) -> {
            if(!(validationResult instanceof ValidatorService.Success valSuccess)) {
                return;
            }
            allOtherExpressions.put(valSuccess.term(), analyze(valSuccess.term()));
        });
        return allOtherExpressions;
    }

    private Map<Dimension, Integer> analyze(UCUMExpression.Term term) {
        CanonicalizerService.CanonicalizationResult canonicalizationResult = service.canonicalize(term);
        if(!(canonicalizationResult instanceof CanonicalizerService.Success success)) {
            return LogUtil.logAndThrow(log, "Failed to canonicalize {}.", term);
        }
        return DimensionAnalyzer.analyze(success.canonicalTerm());
    }
}
