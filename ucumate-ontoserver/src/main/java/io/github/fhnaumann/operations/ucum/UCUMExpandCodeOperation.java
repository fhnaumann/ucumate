package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ExpandCodeOperation;
import io.github.fhnaumann.operations.ucum.filters.ApplyFilter;
import io.github.fhnaumann.operations.ucum.filters.BasePropertyFilter;
import io.github.fhnaumann.operations.ucum.filters.CanonicalFilter;
import io.github.fhnaumann.util.LogUtil;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class UCUMExpandCodeOperation implements ExpandCodeOperation {

    private static final Logger log = LoggerFactory.getLogger(UCUMExpandCodeOperation.class);
    private final Map<String, ApplyFilter> filterProperties;

    private final UCUMService service;

    public UCUMExpandCodeOperation(UCUMService service) {
        this.service = service;
        // may be used in valueset.compose
        this.filterProperties = Map.of(
                "canonical", new CanonicalFilter(service),
                "property", new BasePropertyFilter(service)
        );
    }

    @Override
    public ExpandCodeResult expand(ValueSet valueSet, String textFilter) {
        /*
        The value set may...
        * have codes explicitly listed in compose.include
        * have codes explicitly listed in compose.exclude
        * have filters listed in compose.include
        * have filters listed in compose.exclude
        * have a textFilter to limit returned codes to match it

        The textFilter only searches in "code" and "names".

        same include: intersect
        different include: union
         */
        try {
            Set<UCUMExpression.Term> extractedIncludeTerms = extractExplicitCodesOrFilteredCodes(valueSet.getCompose().getInclude());
            if(extractedIncludeTerms.isEmpty()) {
                // no explicit codes and no include filters have been specified - return every known code instead of nothing.
                // The excludes or text filters may limit the actual returned codes
                extractedIncludeTerms = PluginUtil.getAllKnownValidTerms(service);
            }
            Set<UCUMExpression.Term> extractedExcludeTerms = extractExplicitCodesOrFilteredCodes(valueSet.getCompose().getExclude());
            extractedIncludeTerms.removeAll(extractedExcludeTerms);

            extractedIncludeTerms = applyTextFiltering(textFilter, extractedIncludeTerms);

            return new PerfectSuccess(addToVsExpansion(valueSet, extractedIncludeTerms));
        } catch (ExpandCodeOperationException e) {
            return new Failure(e.getMessage());
        }
    }

    private Set<UCUMExpression.Term> extractExplicitCodesOrFilteredCodes(List<ValueSet.ConceptSetComponent> conceptSetComponents) {
        return conceptSetComponents.stream()
                .map(conceptSetComponent -> extractExplicitCodesOrFilteredCodes(conceptSetComponent, Mode.INTERSECTION))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private ValueSet addToVsExpansion(ValueSet valueSet, Set<UCUMExpression.Term> mergedIncludeTerms) {
        ValueSet.ValueSetExpansionComponent expansion = valueSet.getExpansion();
        expansion.setTotal(mergedIncludeTerms.size());
        List<ValueSet.ValueSetExpansionContainsComponent> containsComponents = mergedIncludeTerms.stream()
                .map(term -> new ValueSet.ValueSetExpansionContainsComponent().setCode(service.print(term, Printer.PrintType.UCUM_SYNTAX)).setDisplay(service.print(term, Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX)))
                .toList();
        expansion.setContains(containsComponents);
        return valueSet;
    }

    private Set<UCUMExpression.Term> applyTextFiltering(String textFilter, Set<UCUMExpression.Term> mergedIncludeTerms) {
        if(textFilter != null) {
            mergedIncludeTerms = mergedIncludeTerms.stream()
                    .filter(term -> service.print(term, Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX).contains(textFilter))
                    .collect(Collectors.toSet());
        }
        return mergedIncludeTerms;
    }


    private Set<UCUMExpression.Term> createBasedOnFilter(String filterType, ValueSet.FilterOperator operator, String expression) throws ExpandCodeOperationException {
        ApplyFilter applyFilter = filterProperties.get(filterType);
        if(applyFilter == null) {
            return LogUtil.logAndThrow(log, ExpandCodeOperationException.class, "Unknown filter type '{}'. Only {} are known filter types.", filterType, filterProperties.keySet());
        }
        try {
            return new HashSet<>(applyFilter.apply(expression, operator));
        } catch (InvalidInputException e) {
            return LogUtil.logAndThrow(log, ExpandCodeOperationException.class, "The input '{}' is invalid and therefore could not be expanded.", expression);
        }
    }

    private Set<UCUMExpression.Term> extractExplicitCodesOrFilteredCodes(ValueSet.ConceptSetComponent conceptSetComponent, Mode mode) {
        if(conceptSetComponent.hasConcept()) {
            return extractExplicitCodes(conceptSetComponent);
        }
        else if(conceptSetComponent.hasFilter()) {
            return extractAndApplyFilters(conceptSetComponent, mode);
        }
        else {
            throw new RuntimeException();
        }
    }

    private Set<UCUMExpression.Term> extractAndApplyFilters(ValueSet.ConceptSetComponent conceptSetComponent, Mode mode) {
        return switch (mode) {
            case UNION -> conceptSetComponent.getFilter().stream()
                    .map(f -> createBasedOnFilter(f.getProperty(), f.getOp(), f.getValue()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            case INTERSECTION -> conceptSetComponent.getFilter().stream()
                    .map(f -> createBasedOnFilter(f.getProperty(), f.getOp(), f.getValue()))
                    .reduce(intersectingReducer())
                    .orElseGet(Set::of);
        };
    }

    private Set<UCUMExpression.Term> extractExplicitCodes(ValueSet.ConceptSetComponent conceptSetComponent) {
        return conceptSetComponent.getConcept().stream()
                .map(ValueSet.ConceptReferenceComponent::getCode)
                .map(service::validate)
                .filter(ValidatorService.Success.class::isInstance)
                .map(ValidatorService.Success.class::cast)
                .map(ValidatorService.Success::term)
                .collect(Collectors.toSet());
    }

    private static BinaryOperator<Set<UCUMExpression.Term>> intersectingReducer() {
        return (terms, terms2) -> {
            Set<UCUMExpression.Term> result = new HashSet<>(terms);
            result.retainAll(terms2);
            return result;
        };
    }

    private enum Mode {
        INTERSECTION, UNION;
    }

    public static class ExpandCodeOperationException extends RuntimeException {
        public ExpandCodeOperationException() {
        }

        public ExpandCodeOperationException(String message) {
            super(message);
        }
    }

}
