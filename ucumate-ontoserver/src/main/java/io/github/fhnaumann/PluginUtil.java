package io.github.fhnaumann;

import au.csiro.ontoserver.OntoOperationPlugin;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.operations.ucum.Unchecked;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.LogUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class PluginUtil {


    private static final Logger log = LoggerFactory.getLogger(PluginUtil.class);

    public static CodeableConcept coding2CodeableConcept(Coding coding) {
        return new CodeableConcept(coding);
    }

    private static UCUMExpression.Term fromUCUMUnit(UCUMDefinition.UCUMUnit unit) {
        return SoloTermBuilder.builder().withoutPrefix(unit).noExpNoAnnot().asTerm().build();
    }

    public static UCUMExpression.Term getBaseUnitFromBaseProperty(String baseProperty, OntoOperationPlugin plugin, UcumVersion ucumVersion) {
        UCUMDefinition.BaseUnit unit = UCUMRegistry.getInstance().getBaseUnits(ucumVersion).stream()
                .filter(baseUnit -> baseUnit.property().equals(baseProperty))
                .findFirst()
                .orElseThrow(() -> new Unchecked.UncheckedUnprocessableEntityException("'%s' is not a known base property.".formatted(baseProperty), plugin));
        return SoloTermBuilder.builder().withoutPrefix(unit).noExpNoAnnot().asTerm().build();
    }

    public static Set<UCUMExpression.Term> getAllKnownValidTerms(UCUMService service) {
        Set<UCUMExpression.Term> fromRegistry = service.getUCUMRegistry().getAll().stream()
                .filter(UCUMDefinition.UCUMUnit.class::isInstance)
                .map(UCUMDefinition.UCUMUnit.class::cast)
                .map(PluginUtil::fromUCUMUnit)
                .collect(Collectors.toSet());
        Set<UCUMExpression.Term> fromStorage = PersistenceRegistry.getInstance().getAllValidated().values().stream()
                .filter(ValidatorService.Success.class::isInstance)
                .map(ValidatorService.Success.class::cast)
                .map(ValidatorService.Success::term)
                .collect(Collectors.toSet());
        return Stream.concat(fromRegistry.stream(), fromStorage.stream()).collect(Collectors.toSet());
    }

    public static Map<UCUMExpression.Term, Map<Dimension, Integer>> analyzeAllKnownValidTerms(UCUMService service) {
        return getAllKnownValidTerms(service).stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        term -> analyze(service, term)
                ));
    }

    public static Map<Dimension, Integer> analyze(UCUMService service, UCUMExpression.Term term) {
        CanonicalizerService.CanonicalizationResult canonicalizationResult = service.canonicalize(term);
        return switch (canonicalizationResult) {
            case CanonicalizerService.Success success -> DimensionAnalyzer.analyze(success.canonicalTerm());
            case ValidatorService.ParserError parserError -> LogUtil.logAndThrow(log, "Failed to canonicalize because of a parsing error: {}.", term);
            case CanonicalizerService.TermContainsPHAndCanonicalizingToMass termContainsPHAndCanonicalizingToMass -> Map.of();
            case CanonicalizerService.TermHasArbitraryUnit termHasArbitraryUnit -> Map.of();
        };
    }
}
