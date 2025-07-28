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

import java.util.Map;
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

    public static Stream<UCUMExpression.Term> getAllKnownValidTerms(UCUMService service) {
        /*
        Getting the terms from the UCUMRegistry does not have to be lazy because they are hard-limited to roughly ~3000 codes
        However, retrieving the values from the persistence layer may be very large, a lazy stream is necessary here
         */
        Stream<UCUMExpression.Term> fromRegistry = service.getUCUMRegistry().getAll().stream()
                .filter(UCUMDefinition.UCUMUnit.class::isInstance)
                .map(UCUMDefinition.UCUMUnit.class::cast)
                .map(PluginUtil::fromUCUMUnit);
        Stream<UCUMExpression.Term> fromStorage = PersistenceRegistry.getInstance().getAllValidatedLazy()
                .map(Map.Entry::getValue)
                .filter(ValidatorService.Success.class::isInstance)
                .map(ValidatorService.Success.class::cast)
                .map(ValidatorService.Success::term);
        return Stream.concat(fromRegistry, fromStorage);
    }

    public static Stream<Map.Entry<UCUMExpression.Term, Map<DimensionType, Integer>>> analyzeAllKnownValidTerms(UCUMService service) {
        return getAllKnownValidTerms(service)
                .map(term -> Map.entry(term, analyze(service, term)));
    }

    public static Map<DimensionType, Integer> analyze(UCUMService service, UCUMExpression.Term term) {
        CanonicalizerService.CanonicalizationResult canonicalizationResult = service.canonicalize(term);
        return switch (canonicalizationResult) {
            case CanonicalizerService.Success success -> DimensionAnalyzer.analyze(success.canonicalTerm());
            case ValidatorService.ParserError parserError -> LogUtil.logAndThrow(log, "Failed to canonicalize because of a parsing error: {}.", term);
            case CanonicalizerService.TermContainsPHAndCanonicalizingToMass termContainsPHAndCanonicalizingToMass -> Map.of();
            case CanonicalizerService.TermHasArbitraryUnit termHasArbitraryUnit -> Map.of();
        };
    }
}
