package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.UCUMOntoOperationPlugin;
import io.github.fhnaumann.builders.CombineTermBuilder;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.UCUMRegistry;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class VSComposeOptimiser {

    private record Dimension(DimensionType type, int exponent) {}
    private record DimensionalSignature(Set<Dimension> dimensions) {}
    private record Filter(Operator operator, Set<DimensionalSignature> signatures) {}
    private record FHIRFilter(Operator operator, Set<UCUMExpression.CanonicalTerm> filterValue) {}
    private enum Operator {
        EQ(ValueSet.FilterOperator.EQUAL),
        IN(ValueSet.FilterOperator.IN);

        private final ValueSet.FilterOperator fhirOperator;

        Operator(ValueSet.FilterOperator filterOperator) {
            this.fhirOperator = filterOperator;
        }
        static Operator fromFHIR(ValueSet.FilterOperator fhirOperator) {
            return switch (fhirOperator) {
                case IN -> IN;
                case EQUAL -> EQ;
                default -> throw new RuntimeException();
            };
        }

    }

    public sealed interface OptimisationResult {}
    public record NotOptimised() implements OptimisationResult {}
    public record OptimisedIncludeConcepts(Set<UCUMExpression.Term> explicitIncludeConcepts) implements OptimisationResult {}
    public record OptimisedCanonicalFilter(boolean include, ValueSet.FilterOperator filterOperator, Set<UCUMExpression.CanonicalTerm> filterValue) implements OptimisationResult {}
    public record OptimisedCanonicalFilterAndIncludeConcepts(boolean include, ValueSet.FilterOperator filterOperator, Set<UCUMExpression.CanonicalTerm> filterValue, Set<UCUMExpression.Term> explicitIncludeConcepts) implements OptimisationResult {}

    private final Map<String, UCUMExpression.CanonicalTerm> knownProperties;

    private final UCUMService service;
    private final UCUMOntoOperationPlugin plugin;

    public VSComposeOptimiser(UCUMOntoOperationPlugin plugin, UCUMService service) {
        this.plugin = plugin;
        this.service = service;
        this.knownProperties = service.getUCUMRegistry().getBaseUnits().stream()
                .collect(Collectors.toMap(
                        UCUMDefinition.BaseUnit::property,
                        baseUnit -> (UCUMExpression.CanonicalTerm) SoloTermBuilder.builder().withoutPrefix(baseUnit).noExpNoAnnot().asTerm().build()
                ));
    }

    public OptimisationResult optimise(ValueSet.ValueSetComposeComponent compose) {
        // if all of UCUM is to be excluded, then there is no optimisation to do
        boolean excludeAllOfUCUM = compose.getExclude().stream()
                .filter(UCUMOntoOperationPlugin.IS_UCUM_SYSTEM)
                .anyMatch(exclude -> !exclude.hasFilter() && !exclude.hasConcept());
        if(excludeAllOfUCUM) {
            return new NotOptimised();
        }

        Set<UCUMExpression.Term> includeConcepts = new HashSet<>();
        Set<UCUMExpression.Term> excludeConcepts = new HashSet<>();
        // 1) Optimise Include Filters
        List<Filter> optimisedIncludeFilters = new ArrayList<>();
        boolean includeAllOfUCUM = false;
        for(ValueSet.ConceptSetComponent include : compose.getInclude()) {
            if(!UCUMOntoOperationPlugin.IS_UCUM_SYSTEM.test(include)) {
                continue;
            }
            List<Filter> filtersInsideInclude = getFiltersInside(include);
            if(!filtersInsideInclude.isEmpty()) {
                Filter optimisedIncludeFilter = optimiseFilters(filtersInsideInclude, this::intersect);
                if(optimisedIncludeFilter != null) {
                    optimisedIncludeFilters.add(optimisedIncludeFilter);
                }
            }
            else {
                Set<UCUMExpression.Term> conceptsInsideInclude = getConceptsInside(include);
                if(!conceptsInsideInclude.isEmpty()) {
                    includeConcepts.addAll(conceptsInsideInclude);
                }
                else {
                    // UCUM System but no include filter or concepts provided -> include all of UCUM
                    includeAllOfUCUM = true;
                }
            }
        }
        // nothing to optimise if all of UCUM is included
        Filter optimisedIncludeFilter = includeAllOfUCUM ? null : optimiseFilters(optimisedIncludeFilters, this::union);
        // 2) Optimise Exclude Filters
        List<Filter> optimisedExcludeFilters = new ArrayList<>();
        for(ValueSet.ConceptSetComponent exclude : compose.getExclude()) {
            if(!UCUMOntoOperationPlugin.IS_UCUM_SYSTEM.test(exclude)) {
                continue;
            }
            List<Filter> filtersInsideExclude = getFiltersInside(exclude);
            if(!filtersInsideExclude.isEmpty()) {
                Filter optimisedExcludeFilter = optimiseFilters(filtersInsideExclude, this::intersect);
                if(optimisedExcludeFilter != null) {
                    optimisedExcludeFilters.add(optimisedExcludeFilter);
                }
            }
            else {
                Set<UCUMExpression.Term> conceptsInsideExclude = getConceptsInside(exclude);
                excludeConcepts.addAll(conceptsInsideExclude);
            }
        }
        Filter optimisedExcludeFilter = optimiseFilters(optimisedExcludeFilters, this::union);
        // 3) "Subtract" optimised Exclude Filter from optimised Include Filter
        Filter optimisedSubtractedFilter = includeAllOfUCUM ? optimisedExcludeFilter : subtract(optimisedIncludeFilter, optimisedExcludeFilter);
        // 4) "Subtract" Exclude Concepts from Include Concepts
        includeConcepts.removeAll(excludeConcepts);
        // 5) Only keep Include Concepts IF their dimension is NOT in the optimised Exclude Filter
        // need to differentiate between "no optimisation" and "optimisation resulted in empty sets"
        if(optimisedExcludeFilter != null) {
            includeConcepts.removeIf(term -> conceptIsCapturedByFilter(term, optimisedExcludeFilter));
        }

        return createResult(optimisedSubtractedFilter, includeAllOfUCUM, includeConcepts);
    }

    private OptimisationResult createResult(Filter optimisedSubtractedFilter, boolean includeAllOfUCUM, Set<UCUMExpression.Term> includeConcepts) {
        if(optimisedSubtractedFilter == null) {
            if(!includeConcepts.isEmpty()) {
                return new OptimisedIncludeConcepts(includeConcepts);
            }
            else {
                return new NotOptimised();
            }
        }
        else {
            if(!includeConcepts.isEmpty()) {
                return new OptimisedCanonicalFilterAndIncludeConcepts(
                        !includeAllOfUCUM,
                        optimisedSubtractedFilter.operator.fhirOperator,
                        constructFilterValueFrom(optimisedSubtractedFilter.signatures),
                        includeConcepts
                );
            }
            else {
                return new OptimisedCanonicalFilter(!includeAllOfUCUM, optimisedSubtractedFilter.operator.fhirOperator, constructFilterValueFrom(optimisedSubtractedFilter.signatures));
            }
        }
    }

    private Set<UCUMExpression.CanonicalTerm> constructFilterValueFrom(Set<DimensionalSignature> signatures) {
        return signatures.stream()
                .map(this::createCanonicalTermFrom)
                .collect(Collectors.toSet());
    }

    private UCUMExpression.CanonicalTerm createCanonicalTermFrom(DimensionalSignature dimensionalSignature) {
        return dimensionalSignature.dimensions.stream()
                .sorted(Comparator.comparing(Dimension::type))
                .map(dimension -> {
                    UCUMDefinition.BaseUnit baseUnit = DimensionType.getBaseUnit(dimension.type, service.getUCUMRegistry());
                    return (UCUMExpression.CanonicalTerm) SoloTermBuilder.builder().withoutPrefix(baseUnit).asComponent().withExponent(dimension.exponent).withoutAnnotation().asTerm().build();
                })
                .reduce(CombineTermBuilder.APPEND_RIGHT_MUL)
                .orElseThrow();
    }

    private boolean conceptIsCapturedByFilter(UCUMExpression.Term concept, Filter filter) {
        return switch (service.canonicalize(concept)) {
            case CanonicalizerService.FailedCanonicalization failedCanonicalization -> throw new Unchecked.UncheckedUnprocessableEntityException("Canonicalization failed for '%s'.".formatted(service.print(concept)), plugin);
            case CanonicalizerService.Success success -> {
                Set<Dimension> conceptDims = fromDimAnalyzer(success.canonicalTerm());
                yield filter.signatures.stream()
                        .anyMatch(dimensionalSignature -> dimensionalSignature.dimensions.equals(conceptDims));
            }
        };
    }

    private Set<UCUMExpression.Term> getConceptsInside(ValueSet.ConceptSetComponent comp) {
        return comp.getConcept().stream()
                .filter(ValueSet.ConceptReferenceComponent::hasCode)
                .map(conceptComp -> switch (service.validate(conceptComp.getCode())) {
                        case ValidatorService.Failure failure -> throw new Unchecked.UncheckedUnprocessableEntityException(String.join(",", failure.errorMessages()), plugin);
                        case ValidatorService.Success success -> success.term();
                    })
                .collect(Collectors.toSet());
    }

    private List<Filter> getFiltersInside(ValueSet.ConceptSetComponent comp) {
        return comp.getFilter().stream()
                .map(filterComp -> {
                    Operator op = Operator.fromFHIR(filterComp.getOp());
                    if(filterComp.getProperty().equals("property")) {
                        if(op != Operator.EQ) {
                            throw new Unchecked.UncheckedUnprocessableEntityException("'%s' operator is not supported for the property filter.".formatted(op), plugin);
                        }
                        return mapPropertyFilterToCanonicalFilter(filterComp.getValue());
                    }
                    else if(filterComp.getProperty().equals("canonical")) {
                        if(op != Operator.EQ && op != Operator.IN) {
                            throw new Unchecked.UncheckedUnprocessableEntityException("'%s' operator is not supported for the canonical filter.".formatted(op), plugin);
                        }
                        return new FHIRFilter(op, parseFilterValue(filterComp.getValue(), op));
                    }
                    else {
                        throw new Unchecked.UncheckedUnprocessableEntityException("Unknown filter '%s' encountered.".formatted(filterComp.getProperty()), plugin);
                    }
                })
                .map(this::modelFHIRFilter)
                .toList();
    }

    private FHIRFilter mapPropertyFilterToCanonicalFilter(String propertyFilterValue) {
        if(!knownProperties.containsKey(propertyFilterValue)) {
            throw new Unchecked.UncheckedUnprocessableEntityException("Unknown base unit property '%s'.".formatted(propertyFilterValue), plugin);
        }
        return new FHIRFilter(Operator.EQ, Set.of(knownProperties.get(propertyFilterValue)));
    }

    private Set<UCUMExpression.CanonicalTerm> parseFilterValue(String filterValue, Operator op) {
        return switch (op) {
            case EQ -> switch (service.canonicalize(filterValue)) {
                case CanonicalizerService.FailedCanonicalization failedCanonicalization -> throw new Unchecked.UncheckedUnprocessableEntityException("Canonicalization failed for '%s'.".formatted(filterValue), plugin);
                case CanonicalizerService.Success success -> Set.of(success.canonicalTerm());
            };
            case IN -> Arrays.stream(filterValue.split(","))
                    .map(s -> parseFilterValue(s, Operator.EQ))
                    .reduce((canonicalTerms, canonicalTerms2) -> {
                        Set<UCUMExpression.CanonicalTerm> result = new HashSet<>();
                        result.addAll(canonicalTerms);
                        result.addAll(canonicalTerms2);
                        return result;
                    })
                    .orElseThrow();
        };
    }

    private Filter optimiseFilters(List<Filter> filters, BinaryOperator<Filter> binaryOperator) {
        return filters.stream()
                .reduce(binaryOperator)
                .orElse(null);
    }

    private Filter intersect(Filter left, Filter right) {
        Set<DimensionalSignature> leftCopy = new HashSet<>(left.signatures);
        leftCopy.retainAll(right.signatures);
        return new Filter(determineOperator(leftCopy), leftCopy);
    }

    private Filter union(Filter left, Filter right) {
        Set<DimensionalSignature> leftCopy = new HashSet<>(left.signatures);
        leftCopy.addAll(right.signatures);
        return new Filter(determineOperator(leftCopy), leftCopy);
    }

    private Filter subtract(Filter include, Filter exclude) {
        if(include == null) {
            return null;
        }
        if(exclude == null) {
            return include;
        }
        Set<DimensionalSignature> result = new HashSet<>(include.signatures);
        result.removeAll(exclude.signatures);
        return new Filter(determineOperator(result), result);
    }

    private Operator determineOperator(Set<DimensionalSignature> signatures) {
        return signatures.size() == 1 ? Operator.EQ : Operator.IN;
    }

    private Filter modelFHIRFilter(FHIRFilter fhirFilter) {
        Set<DimensionalSignature> signatures = fhirFilter.filterValue.stream()
                .map(this::fromDimAnalyzer)
                .map(DimensionalSignature::new)
                .collect(Collectors.toSet());
        return new Filter(fhirFilter.operator, signatures);
    }

    private Set<Dimension> fromDimAnalyzer(UCUMExpression.CanonicalTerm term) {
        Map<DimensionType, Integer> dimMap = DimensionAnalyzer.analyze(term);
        return dimMap.entrySet().stream()
                .map(entry -> new Dimension(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }
}
