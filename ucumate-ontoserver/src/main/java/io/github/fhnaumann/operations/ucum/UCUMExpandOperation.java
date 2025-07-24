package io.github.fhnaumann.operations.ucum;
import au.csiro.ontoserver.exceptions.PluginBaseException;
import au.csiro.ontoserver.operations.Processor;
import au.csiro.ontoserver.operations.expand.CodeSystemVersionPair;
import au.csiro.ontoserver.operations.expand.ExpandOperation;
import au.csiro.ontoserver.operations.expand.ExpansionProcessor;
import au.csiro.ontoserver.operations.expand.ExpansionProfile;
import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.UCUMOntoOperationPlugin;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.CustomUnitMappingPrinter;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ucum.filters.ApplyFilter;
import io.github.fhnaumann.operations.ucum.filters.BasePropertyFilter;
import io.github.fhnaumann.operations.ucum.filters.CanonicalFilter;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the $expand operation for UCUM ValueSets.
 * @author Felix Naumann
 */
public class UCUMExpandOperation implements ExpandOperation {

    private static final Logger log = LoggerFactory.getLogger(UCUMExpandOperation.class);
    private final Map<String, ApplyFilter> filterProperties;

    private final UCUMOntoOperationPlugin plugin;
    private final UCUMService service;

    public UCUMExpandOperation(UCUMOntoOperationPlugin plugin, UCUMService service) {
        this.plugin = plugin;
        this.service = service;
        // may be used in valueset.compose
        this.filterProperties = Map.of(
                "canonical", new CanonicalFilter(plugin, service),
                "property", new BasePropertyFilter(plugin, service)
        );
    }


    @Override
    public void expand(ValueSet valueSet, ExpansionProfile expansionProfile, ExpansionProcessor expansionProcessor) throws PluginBaseException {
        try {
            // todo maybe do this in a smarter way by combining include and exclude first before getting all Terms for them
            // similar to how its done in validate-code

            // Process all includes - should be UNION of all include blocks
            Set<UCUMExpression.Term> extractedIncludeTerms = new HashSet<>();
            for (ValueSet.ConceptSetComponent include : valueSet.getCompose().getInclude()) {
                Set<UCUMExpression.Term> blockTerms = extractExplicitCodesOrFilteredCodes(include, expansionProcessor);
                extractedIncludeTerms.addAll(blockTerms); // UNION operation
            }

            // Process all excludes - should be UNION of all exclude blocks
            Set<UCUMExpression.Term> extractedExcludeTerms = new HashSet<>();
            for (ValueSet.ConceptSetComponent exclude : valueSet.getCompose().getExclude()) {
                Set<UCUMExpression.Term> blockTerms = extractExplicitCodesOrFilteredCodes(exclude, expansionProcessor);
                extractedExcludeTerms.addAll(blockTerms); // UNION operation
            }

            // Remove excludes from includes
            extractedIncludeTerms.removeAll(extractedExcludeTerms);

            extractedIncludeTerms = applyTextFiltering(expansionProfile.filter(), extractedIncludeTerms);

            List<ValueSet.ValueSetExpansionContainsComponent> results = extractedIncludeTerms.stream().map(this::terms2ExpansionComp).toList();

            Stream<CodeSystemVersionPair> codeSystemVersionPairStream = getPairsFromResults(results);
            expansionProcessor.codeSystemVersionPairs(codeSystemVersionPairStream);

            if(!results.isEmpty()) {
                // todo this might be problematic for a lazy stream implementation. Checking the size does not work with laziness, but there has to be a way
                // to differentiate between "the plugin determined that there 0 results" and "the plugin could not determine anything".
                expansionProcessor.results(results.stream(), (long) results.size());
            }
        } catch (WrappingCheckedException e) {
            throw e.getUnderlyingException();
        }
    }

    private Stream<CodeSystemVersionPair> getPairsFromResults(List<ValueSet.ValueSetExpansionContainsComponent> results) {
        // This plugin only handles the UCUM CodeSystem but manages multiple versions (if different versions are used throughout the VS)
        return results.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                ValueSet.ValueSetExpansionContainsComponent::getVersion,
                                // UCUM CodeSystem is case sensitive
                                comp -> new CodeSystemVersionPair(comp.getSystem(), comp.getVersion(), true), // comp.getSystem() should always be the UCUM system
                                (existing, replacement) -> existing
                        ),
                        map -> map.values().stream()
                ));
    }

    public boolean includesAllUCUMCodes(ValueSet valueSet, Processor processor) {
        Set<UCUMExpression.Term> extractedIncludeTerms = extractExplicitCodesOrFilteredCodes(valueSet.getCompose().getInclude(), processor);
        return extractedIncludeTerms.isEmpty();
    }

    private OperationOutcome constructOutcomeFrom(ExpandCodeOperationException e) {
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(e.isParserException() ? OperationOutcome.IssueType.CODEINVALID : OperationOutcome.IssueType.NOTSUPPORTED)
                .setDetails(new CodeableConcept().setText(e.getMessage()));
        return operationOutcome;
    }

    private Set<UCUMExpression.Term> extractExplicitCodesOrFilteredCodes(List<ValueSet.ConceptSetComponent> conceptSetComponents, Processor processor) {
        return conceptSetComponents.stream()
                .map(conceptSetComponent -> extractExplicitCodesOrFilteredCodes(conceptSetComponent, processor))
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

    private ValueSet.ValueSetExpansionContainsComponent terms2ExpansionComp(UCUMExpression.Term term) {
        ValueSet.ValueSetExpansionContainsComponent expansionComp = new ValueSet.ValueSetExpansionContainsComponent();
        String code = service.print(term, Printer.PrintType.UCUM_SYNTAX);
        expansionComp.setCode(code);
        expansionComp.setDisplay(code);
        expansionComp.setVersion(service.getUCUMVersion().getVersion());
        expansionComp.setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        return expansionComp;
    }

    private Set<UCUMExpression.Term> applyTextFiltering(String textFilter, Set<UCUMExpression.Term> mergedIncludeTerms) {
        if(textFilter != null) {
            CustomUnitMappingPrinter customUnitMappingPrinter = new CustomUnitMappingPrinter(concept -> String.join(",", concept.names()));
            mergedIncludeTerms = mergedIncludeTerms.stream()
                    .filter(term -> service.print(term, customUnitMappingPrinter).contains(textFilter))
                    .collect(Collectors.toSet());
        }
        return mergedIncludeTerms;
    }


    private Set<UCUMExpression.Term> createBasedOnFilter(String filterType, ValueSet.FilterOperator operator, String expression) {
        ApplyFilter applyFilter = filterProperties.get(filterType);
        if(applyFilter == null) {
            throw new Unchecked.UncheckedUnprocessableEntityException("Unknown filter '%s'".formatted(filterType), plugin);
            //return LogUtil.logAndThrow(log, ExpandCodeOperationException.class, "Unknown filter type '{}'. Only {} are known filter types.", filterType, filterProperties.keySet());
        }
        return new HashSet<>(applyFilter.apply(expression, operator));
    }

    private Set<UCUMExpression.Term> extractExplicitCodesOrFilteredCodes(ValueSet.ConceptSetComponent conceptSetComponent, Processor processor) {
        if(!UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(conceptSetComponent.getSystem())) {
            return Set.of();
        }
        if(conceptSetComponent.hasConcept()) {
            return extractExplicitCodes(conceptSetComponent);
        }
        else if(conceptSetComponent.hasFilter()) {
            return extractAndApplyIntersectionFilters(conceptSetComponent, processor);
        }
        else {
            // UCUM CodeSystem with no concept or filter means get all known codes
            // The text filter in the query may further limit the actual returned codes
            return PluginUtil.getAllKnownValidTerms(service).collect(Collectors.toSet()); // todo keep laziness
        }
    }

    private Set<UCUMExpression.Term> extractAndApplyIntersectionFilters(ValueSet.ConceptSetComponent conceptSetComponent, Processor processor) {
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
            case ValidatorService.Failure failure -> throw new Unchecked.UncheckedUnprocessableEntityException(String.join(",", failure.errorMessages()), plugin);
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
