package io.github.fhnaumann.operations.ucum.filters;

import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ucum.InvalidInputException;
import io.github.fhnaumann.operations.ucum.UCUMExpandCodeOperation;
import io.github.fhnaumann.util.LogUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class BasePropertyFilter implements ApplyFilter {

    private static final Set<String> KNOWN_PROPERTIES = UCUMRegistry.getInstance().getBaseUnits(ConfigurationRegistry.get().getUCUMVersionAsEnum()).stream()
            .map(UCUMDefinition.BaseUnit::property)
            .collect(Collectors.toSet());
    private static final Logger log = LoggerFactory.getLogger(BasePropertyFilter.class);

    private final UCUMService service;
    private final Extractor extractor;

    public BasePropertyFilter(UCUMService service) {
        this.service = service;
        this.extractor = new Extractor(service);
    }

    @Override
    public Collection<UCUMExpression.Term> apply(String propertyName, ValueSet.FilterOperator operator) throws InvalidInputException {
        if(!KNOWN_PROPERTIES.contains(propertyName)) {
            return LogUtil.logAndThrow(log, UCUMExpandCodeOperation.ExpandCodeOperationException.class, "Unknown property '{}'. Only {} are known properties.", propertyName, KNOWN_PROPERTIES);
        }
        return switch (operator) {
            case EQUAL -> handleEqual(propertyName);
            default -> LogUtil.logAndThrow(log, UCUMExpandCodeOperation.ExpandCodeOperationException.class, "Only '=' (equal) is supported on the property filter.");
        };
    }

    private Collection<UCUMExpression.Term> handleEqual(String propertyName) {
        return PluginUtil.getAllKnownValidTerms(service).stream()
                .filter(this::isComparableToAnyBaseUnit)
                .filter(term -> matchesExtraction(term, propertyName))
                .toList();

    }

    private boolean isComparableToAnyBaseUnit(UCUMExpression.Term term) {
        /*
        It is considered comparable if the term is a single unit (no mul or div) with no exponent.
        Exception: Mul with dimensionless units (or integer numbers), or div with dimensionless units where the unit is in the numerator, is fine
        Canonicalizing the input will result in a ComponentTerm if there are no exponents, etc.
        The edge case mentioned above will automatically resolve itself because during canonicalization (if possible), it will
        be factored out into the magnitude.
         */
        CanonicalizerService.CanonicalizationResult canonResult = service.canonicalize(term);
        return switch (canonResult) {
            case CanonicalizerService.TermHasArbitraryUnit termHasArbitraryUnit -> false;
            case CanonicalizerService.TermContainsPHAndCanonicalizingToMass termContainsPHAndCanonicalizingToMass -> false;
            case ValidatorService.ParserError parserError -> LogUtil.logAndThrow(log, "Failed to canonicalize {} because of a parser error: {}", term, parserError);
            case CanonicalizerService.Success success -> {
                if(!(success.canonicalTerm() instanceof UCUMExpression.ComponentTerm componentTerm)) {
                    yield false;
                }
                Map<Dimension, Integer> dimAnalysis = DimensionAnalyzer.analyze(success.canonicalTerm());
                yield !dimAnalysis.containsKey(Dimension.NO_DIMENSION) && dimAnalysis.values().stream().noneMatch(integer -> integer < 0);
            }
        };
    }

    private boolean matchesExtraction(UCUMExpression.Term term, String propertyName) {
        List<String> extractedProperties = extractor.extractFrom(term, UCUMDefinition.UCUMUnit::property);
        if(extractedProperties.isEmpty()) {
            return LogUtil.logAndThrow(log, "Extracted no properties from {} while there should have been exactly one.", service.print(term));
        }
        /*
        There may be more than one extracted property because of dimensionless units such as "mol" or "pi".
        They pass the previous checks because they canonicalize down to dimensionless (with a factor), but unlike integer
        units, they also have a property. They can be safely ignored here because they themselves are "invisible" to
        the base property filter. They cannot be explicitly searched for because they have no associated base dimension
        like meter<->mass has. But when a dimensionless unit exists within an expression like "kg/mol" then the search
        for "mass" will match this unit
         */
        return extractedProperties.contains(propertyName);
    }
}
