package io.github.fhnaumann.operations.ucum;
import au.csiro.ontoserver.exceptions.PluginBaseException;
import au.csiro.ontoserver.operations.Processor;
import au.csiro.ontoserver.operations.expand.CodeSystemVersionPair;
import au.csiro.ontoserver.operations.expand.ExpandOperation;
import au.csiro.ontoserver.operations.expand.ExpansionProcessor;
import au.csiro.ontoserver.operations.expand.ExpansionProfile;
import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.UCUMOntoOperationPlugin;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.printer.CustomUnitMappingPrinter;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the $expand operation for UCUM ValueSets.
 * @author Felix Naumann
 */
public class UCUMExpandOperation implements ExpandOperation {

    private static final Logger log = LoggerFactory.getLogger(UCUMExpandOperation.class);

    private final UCUMOntoOperationPlugin plugin;
    private final UCUMService service;
    private final VSComposeOptimiser vsComposeOptimiser;
    private final Extractor extractor;

    public UCUMExpandOperation(UCUMOntoOperationPlugin plugin, UCUMService service) {
        this.plugin = plugin;
        this.service = service;
        this.vsComposeOptimiser = new VSComposeOptimiser(plugin, service);
        this.extractor = new Extractor(service);
    }

    public Stream<UCUMExpression.Term> applyOptimisedFilter(Set<UCUMExpression.CanonicalTerm> filterValue) {
        Set<Map<DimensionType, Integer>> filterValueDims = filterValue.stream()
                .map(DimensionAnalyzer::analyze)
                .collect(Collectors.toSet());
        return PluginUtil.analyzeAllKnownValidTerms(service)
                .filter(termMapEntry -> filterValueDims.stream().anyMatch(filterValueDimsMap -> filterValueDimsMap.equals(termMapEntry.getValue())))
                .map(Map.Entry::getKey);
    }

    @Override
    public void expand(ValueSet valueSet, ExpansionProfile expansionProfile, ExpansionProcessor expansionProcessor) throws PluginBaseException {
        if(!valueSet.hasCompose()) {
            return;
        }
        try {

            VSComposeOptimiser.OptimisationResult optimisationResult = vsComposeOptimiser.optimise(valueSet.getCompose());
            Stream<UCUMExpression.Term> result = switch (optimisationResult) {
                case VSComposeOptimiser.Empty empty -> null;
                case VSComposeOptimiser.ExcludeAllOfUCUM excludeAllOfUCUM -> null;
                case VSComposeOptimiser.IncludeAllOfUCUM includeAllOfUCUM -> PluginUtil.getAllKnownValidTerms(service);
                case VSComposeOptimiser.OptimisedConcepts optimisedConcepts -> handleOptimisedConcepts(optimisedConcepts.include(), optimisedConcepts.explicitConcepts());
                case VSComposeOptimiser.OptimisedCanonicalFilter optimisedCanonicalFilter -> applyOptimisedFilter(optimisedCanonicalFilter.filterValue());
                case VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts both -> {
                    Stream<UCUMExpression.Term> explicitConcepts = handleOptimisedConcepts(both.include(), both.explicitIncludeConcepts());
                    Stream<UCUMExpression.Term> filterConcepts = applyOptimisedFilter(both.filterValue());
                    yield Stream.concat(explicitConcepts, filterConcepts);
                }
            };
            if(result == null) {
                expansionProcessor.results(Stream.empty(), 0L);
                return;
            }
            if(expansionProfile.filter() != null && !expansionProfile.filter().isBlank()) {
                result = result.filter(term -> applyTextFilter(term, expansionProfile.filter()));
            }
            if(expansionProfile.offset() != null) {

            }
            if(expansionProfile.count() != null) {

            }

            expansionProcessor.results(asExpansionContainsComp(result));
        } catch (WrappingCheckedException e) {
            throw e.getUnderlyingException();
        }
    }

    private Stream<UCUMExpression.Term> handleOptimisedConcepts(boolean include, Set<UCUMExpression.Term> explicitConcepts) {
        Predicate<UCUMExpression.Term> outExplicitConcepts = explicitConcepts::contains;
        if(include) {
            // the concepts here are the only ones allowed
            return PluginUtil.getAllKnownValidTerms(service)
                    .filter(outExplicitConcepts);
        }
        else {
            // the concepts here are the only ones NOT allowed
            return PluginUtil.getAllKnownValidTerms(service)
                    .filter(Predicate.not(outExplicitConcepts));
        }
    }

    private Stream<ValueSet.ValueSetExpansionContainsComponent> asExpansionContainsComp(Stream<UCUMExpression.Term> terms) {
        return terms.map(term -> service.print(term, Printer.PrintType.UCUM_SYNTAX))
                .map(term -> {
                    ValueSet.ValueSetExpansionContainsComponent cc = new ValueSet.ValueSetExpansionContainsComponent();
                    cc.setCode(term);
                    cc.setDisplay(term);
                    cc.setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
                    cc.setVersion(service.getUCUMVersion().getVersion());
                    return cc;
                });
    }

    private boolean applyTextFilter(UCUMExpression.Term term, String filter) {
        return extractor.extractFrom(term, UCUMDefinition.Concept::names).stream()
                .flatMap(Collection::stream)
                .anyMatch(s -> s.contains(filter));
    }
}
