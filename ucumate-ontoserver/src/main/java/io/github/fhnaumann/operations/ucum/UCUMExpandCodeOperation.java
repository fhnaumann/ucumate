package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ExpandCodeOperation;
import io.github.fhnaumann.operations.ucum.filters.ApplyFilter;
import io.github.fhnaumann.operations.ucum.filters.CanonicalFilter;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class UCUMExpandCodeOperation implements ExpandCodeOperation {

    private final Map<String, ApplyFilter> filterProperties;

    private final UCUMService service;

    private UCUMExpandCodeOperation(UCUMService service) {
        this.service = service;
        // may be used in valueset.compose
        this.filterProperties = Map.of(
                "canonical", new CanonicalFilter(service)
        );
    }

    @Override
    public ValueSet expand(ValueSet valueSet, String textFilter) {
        /*
        The value set may...
        * have codes explicitly listed in compose.include
        * have codes explicitly listed in compose.exclude
        * have filters listed in compose.include
        * have filters listed in compose.exclude
        * have a textFilter to limit returned codes to match it

        The textFilter only searches in "code" and "names".
         */

        Set<UCUMExpression.Term> explicitExcludeTerms = extractExcludeExplicitCodes(valueSet);
        Set<UCUMExpression.Term> expandedExcludeTerms = valueSet.getCompose().getExclude().stream()
                .flatMap(conceptSetComponent -> conceptSetComponent.getFilter().stream())
                .flatMap(conceptSetFilterComponent -> createBasedOnFilter(conceptSetFilterComponent.getProperty(), conceptSetFilterComponent.getOp(), conceptSetFilterComponent.getValue()).stream())
                .collect(Collectors.toSet());
        Set<UCUMExpression.Term> mergedExcludeTerms = new HashSet<>();
        mergedExcludeTerms.addAll(explicitExcludeTerms);
        mergedExcludeTerms.addAll(expandedExcludeTerms);

        Set<UCUMExpression.Term> explicitIncludeTerms = extractIncludeExplicitCodes(valueSet);
        Set<UCUMExpression.Term> expandedIncludeTerms = valueSet.getCompose().getInclude().stream()
                .flatMap(conceptSetComponent -> conceptSetComponent.getFilter().stream())
                .flatMap(conceptSetFilterComponent -> createBasedOnFilter(conceptSetFilterComponent.getProperty(), conceptSetFilterComponent.getOp(), conceptSetFilterComponent.getValue()).stream())
                .collect(Collectors.toSet());
        Set<UCUMExpression.Term> mergedIncludeTerms = new HashSet<>();
        mergedIncludeTerms.addAll(explicitIncludeTerms);
        mergedIncludeTerms.addAll(expandedIncludeTerms);


        mergedIncludeTerms.removeAll(mergedExcludeTerms);

        ValueSet.ValueSetExpansionComponent expansion = valueSet.getExpansion();
        expansion.setTotal(mergedIncludeTerms.size());
        List<ValueSet.ValueSetExpansionContainsComponent> containsComponents = mergedIncludeTerms.stream()
                .map(term -> new ValueSet.ValueSetExpansionContainsComponent().setCode(service.print(term, Printer.PrintType.UCUM_SYNTAX)).setDisplay(service.print(term, Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX)))
                .toList();
        expansion.setContains(containsComponents);
        return valueSet;
    }

    private Set<UCUMExpression.Term> createBasedOnFilter(String filterType, ValueSet.FilterOperator operator, String expression) {
        ApplyFilter applyFilter = filterProperties.get(filterType);
        if(applyFilter == null) {
            return Set.of(); // todo: silently fail if unknown filter?
        }
        try {
            return new HashSet<>(applyFilter.apply(expression, operator));
        } catch (InvalidInputException e) {
            return Set.of(); // todo: silently fail if parsing failed?
        }
    }

    private Set<UCUMExpression.Term> extractIncludeExplicitCodes(ValueSet valueSet) {
        return extractExplicitCodes(valueSet.getCompose().getInclude());

    }

    private Set<UCUMExpression.Term> extractExcludeExplicitCodes(ValueSet valueSet) {
        return extractExplicitCodes(valueSet.getCompose().getExclude());
    }

    private Set<UCUMExpression.Term> extractExplicitCodes(List<ValueSet.ConceptSetComponent> conceptSetComponents) {
        return conceptSetComponents.stream()
                .flatMap(conceptSetComponent -> conceptSetComponent.getConcept().stream())
                .map(ValueSet.ConceptReferenceComponent::getCode)
                .map(service::validate)
                .filter(ValidatorService.Success.class::isInstance)
                .map(ValidatorService.Success.class::cast)
                .map(ValidatorService.Success::term)
                .collect(Collectors.toSet());
    }

}
