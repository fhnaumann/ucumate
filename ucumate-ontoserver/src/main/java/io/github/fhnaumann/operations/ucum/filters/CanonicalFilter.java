package io.github.fhnaumann.operations.ucum.filters;

import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ucum.InvalidInputException;
import io.github.fhnaumann.operations.ucum.UCUMExpandOperation;
import io.github.fhnaumann.util.LogUtil;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
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
            default -> LogUtil.logAndThrow(log, UCUMExpandOperation.ExpandCodeOperationException.class, "The operator '{}' is not supported for UCUM valuesets. Only '{}' are supported.", operator, List.of(ValueSet.FilterOperator.EQUAL, ValueSet.FilterOperator.IN));
        };
    }

    private List<UCUMExpression.Term> applyWithInFilter(UCUMExpression.Term term) {
        return LogUtil.logAndThrow(log, UCUMExpandOperation.ExpandCodeOperationException.class, "'IN' operator is not yet implemented.");
    }

    private List<UCUMExpression.Term> applyWithEqualFilter(UCUMExpression.Term term) {
        Map<Dimension, Integer> sourceDims = PluginUtil.analyze(service, term);
        Map<UCUMExpression.Term, Map<Dimension, Integer>> allOtherExpressions = PluginUtil.analyzeAllKnownValidTerms(service);
        return allOtherExpressions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(sourceDims))
                .map(Map.Entry::getKey)
                .toList();
    }

    ;
}
