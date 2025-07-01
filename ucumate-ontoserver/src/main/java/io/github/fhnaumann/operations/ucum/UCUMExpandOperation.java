package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ExpandOperation;
import io.github.fhnaumann.operations.ucum.filters.ApplyFilter;
import io.github.fhnaumann.operations.ucum.filters.BasePropertyFilter;
import io.github.fhnaumann.operations.ucum.filters.CanonicalFilter;
import io.github.fhnaumann.util.LogUtil;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Inherited;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * Implementation of the $expand operation for UCUM ValueSets.
 * @author Felix Naumann
 */
public class UCUMExpandOperation implements ExpandOperation {

    private static final Logger log = LoggerFactory.getLogger(UCUMExpandOperation.class);
    private final Map<String, ApplyFilter> filterProperties;

    private final UCUMService service;

    public UCUMExpandOperation(UCUMService service) {
        this.service = service;
        // may be used in valueset.compose
        this.filterProperties = Map.of(
                "canonical", new CanonicalFilter(service),
                "property", new BasePropertyFilter(service)
        );
    }

    /**
     * {@inheritDoc}
     * <br>
     * Since there are infinitely many UCUM terms, the expand operation is limited to the union of the units defined by
     * UCUM itself (~3000 <i>simple units</i>) and any encountered UCUM expression in the past of this app instance.
     * <br>
     * If no include filters or concepts are defined, then all known codes are used.
     * @param valueSet The ValueSet to be expanded. Expected to be normalized and only to contain codes from one system.
     * @param textFilter Additional text filter that can be applied to filter the expanded codes.
     * @return
     */
    @Override
    public ExpandResult expand(ValueSet valueSet, String textFilter) {
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
            return new Failure(constructOutcomeFrom(e));
        }
    }

    private OperationOutcome constructOutcomeFrom(ExpandCodeOperationException e) {
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(e.isParserException() ? OperationOutcome.IssueType.CODEINVALID : OperationOutcome.IssueType.NOTSUPPORTED)
                .setDetails(new CodeableConcept().setText(e.getMessage()));
        return operationOutcome;
    }

    private Set<UCUMExpression.Term> extractExplicitCodesOrFilteredCodes(List<ValueSet.ConceptSetComponent> conceptSetComponents) {
        return conceptSetComponents.stream()
                .map(this::extractExplicitCodesOrFilteredCodes)
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
            throw new ExpandCodeOperationException("The input '%s' is invalid and therefore could not be expanded.".formatted(expression), true);
        }
    }

    private Set<UCUMExpression.Term> extractExplicitCodesOrFilteredCodes(ValueSet.ConceptSetComponent conceptSetComponent) {
        if(conceptSetComponent.hasConcept()) {
            return extractExplicitCodes(conceptSetComponent);
        }
        else if(conceptSetComponent.hasFilter()) {
            return extractAndApplyIntersectionFilters(conceptSetComponent);
        }
        else {
            throw new RuntimeException();
        }
    }

    private Set<UCUMExpression.Term> extractAndApplyIntersectionFilters(ValueSet.ConceptSetComponent conceptSetComponent) {
        return conceptSetComponent.getFilter().stream()
                .map(f -> createBasedOnFilter(f.getProperty(), f.getOp(), f.getValue()))
                .reduce(intersectingReducer())
                .orElseGet(Set::of);
    }

    private Set<UCUMExpression.Term> extractExplicitCodes(ValueSet.ConceptSetComponent conceptSetComponent) {
        return conceptSetComponent.getConcept().stream()
                .map(ValueSet.ConceptReferenceComponent::getCode)
                .map(service::validate)
                .map(this::extractSuccessOrThrow)
                .collect(Collectors.toSet());
    }

    private UCUMExpression.Term extractSuccessOrThrow(ValidatorService.ValidationResult result) {
        return switch (result) {
            case ValidatorService.Success success -> success.term();
            case ValidatorService.Failure failure -> throw new ExpandCodeOperationException(String.join(",", failure.errorMessages()), true);
        };
    }


    private static BinaryOperator<Set<UCUMExpression.Term>> intersectingReducer() {
        return (terms, terms2) -> {
            Set<UCUMExpression.Term> result = new HashSet<>(terms);
            result.retainAll(terms2);
            return result;
        };
    }

    public static class ExpandCodeOperationException extends RuntimeException {

        private final boolean parserException;

        public ExpandCodeOperationException() {
            this.parserException = false;
        }

        public ExpandCodeOperationException(String message) {
            this(message, false);
        }

        public ExpandCodeOperationException(String message, boolean parserException) {
            super(message);
            this.parserException = parserException;
        }

        public boolean isParserException() {
            return parserException;
        }
    }

}
