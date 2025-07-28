package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.util.PreciseDecimal;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class Lookup implements LookupService {

    private static final Logger log = LoggerFactory.getLogger(Lookup.class);

    private final VersionSpecificUCUMRegistry registry;

    private static final Set<String> WARN_ON_ENCOUNTER = Set.of(".", "/", "+", "-");
    private final Map<String, UCUMDefinition.UCUMUnit> unitsNormallyWithSbWithoutSb;

    private UcumVersion ucumVersion;
    private final Validator validator;

    public Lookup(UcumVersion ucumVersion) {
        this.ucumVersion = ucumVersion;
        this.validator = new Validator(ucumVersion);
        this.registry = UCUMRegistry.getInstance().getVersionSpecificUCUMRegistry(ucumVersion);
        this.unitsNormallyWithSbWithoutSb = removeSbFromSbUnits();
    }

    private Map<String, UCUMDefinition.UCUMUnit> removeSbFromSbUnits() {
        return registry.getDefinedUnits().stream()
                .filter(definedUnit -> definedUnit.code().contains("[") && definedUnit.code().contains("]"))
                .collect(Collectors.toMap(
                        definedUnit -> definedUnit.code().replace("[", "").replace("]", ""),
                        Function.identity()
                ));
    }

    @Override
    public LookupResult lookup(String input, Collection<MatchType> allowedMatchTypes, Comparator<MatchType> comparator) {



        if(WARN_ON_ENCOUNTER.stream().anyMatch(input::contains)) {
            log.warn("Detected a symbol that indicates the presence of multiple units in the provided input '{}'. " +
                    "Lookup only supports individual units (no prefix or exponent)." +
                    "This method will try to match it to any source definition value instead.", input);
        }
        if(allowedMatchTypes.contains(BuiltInMatchType.CODE)) {
            Optional<UCUMDefinition.Concept> optDirectConceptMatch = registry.getConcept(input);
            if(optDirectConceptMatch.isPresent() && optDirectConceptMatch.get() instanceof UCUMDefinition.UCUMUnit unit) {
                return new DirectMatch(unit);
            }
        }
        List<Map.Entry<UCUMDefinition.UCUMUnit, MatchType>> matches = findAndSortMatches(input, allowedMatchTypes, comparator);
        if(matches.isEmpty()) {
            return new NoMatch();
        }
        return new MultipleMatches(matches.stream().map(Map.Entry::getKey).toList());
    }

    private List<Map.Entry<UCUMDefinition.UCUMUnit, MatchType>> findAndSortMatches(String input, Collection<MatchType> allowedMatchTypes, Comparator<MatchType> comparator) {
        List<Map.Entry<UCUMDefinition.UCUMUnit, MatchType>> matches = new ArrayList<>();

        if(allowedMatchTypes.contains(BuiltInMatchType.CODE_NO_SB_DIRECT) && unitsNormallyWithSbWithoutSb.containsKey(input)) {
            matches.add(Map.entry(unitsNormallyWithSbWithoutSb.get(input), BuiltInMatchType.CODE_NO_SB_DIRECT));
        }

        String unescaped = normalize(input);

        for(UCUMDefinition.Concept concept : registry.getAll()) {
            if(!(concept instanceof UCUMDefinition.UCUMUnit unit)) {
                continue;
            }
            boolean nameDirect = false, printSymbolDirect = false, propertyDirect = false;
            if(allowedMatchTypes.contains(BuiltInMatchType.CODE_CI) && unit.codeCaseInsensitive().equals(input)) {
                matches.add(Map.entry(unit, BuiltInMatchType.CODE_CI));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.NAME_DIRECT) && unit.names().stream().map(this::normalize).anyMatch(s -> s.equals(unescaped))) {
                nameDirect = true;
                matches.add(Map.entry(unit, BuiltInMatchType.NAME_DIRECT));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.NAME_CONTAINS) && !nameDirect && unit.names().stream().map(this::normalize).anyMatch(s -> s.contains(unescaped))) {
                matches.add(Map.entry(unit, BuiltInMatchType.NAME_CONTAINS));
            }
            String normalizedPrintSymbol = normalize(concept.printSymbol());
            if(allowedMatchTypes.contains(BuiltInMatchType.PRINT_SYMBOL_DIRECT) && unescaped.equals(normalizedPrintSymbol)) {
                printSymbolDirect = true;
                matches.add(Map.entry(unit, BuiltInMatchType.PRINT_SYMBOL_DIRECT));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.PRINT_SYMBOL_CONTAINS) && !printSymbolDirect && normalizedPrintSymbol != null && normalizedPrintSymbol.contains(unescaped)) {
                matches.add(Map.entry(unit, BuiltInMatchType.PRINT_SYMBOL_CONTAINS));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.PROPERTY_DIRECT) && input.equals(unit.property())) {
                propertyDirect = true;
                matches.add(Map.entry(unit, BuiltInMatchType.PROPERTY_DIRECT));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.PROPERTY_CONTAINS) && !propertyDirect && unit.property() != null && unit.property().contains(input)) {
                matches.add(Map.entry(unit, BuiltInMatchType.PROPERTY_CONTAINS));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.BASE_DIM) && unit instanceof UCUMDefinition.BaseUnit baseUnit && input.equals(baseUnit.dim())) {
                matches.add(Map.entry(unit, BuiltInMatchType.BASE_DIM));
            }
            if(allowedMatchTypes.contains(BuiltInMatchType.VALUE) && unit instanceof UCUMDefinition.DefinedUnit definedUnit && matchesSourcesDefinition(definedUnit.value(), input)) {
                matches.add(Map.entry(unit, BuiltInMatchType.VALUE));
            }
        }
        matches.sort(Map.Entry.comparingByValue(comparator));
        return matches;
    }

    private boolean matchesSourcesDefinition(UCUMDefinition.UCUMValue value, String input) {
        if(!PreciseDecimal.ONE.equals(value.conversionFactor())) {
            // cannot match any source definition that uses something else than 1 in its definition
            return false;
        }
        return input.equals(value.unit()) || input.equals(value.unitAlt());
    }

    private String normalize(String string) {
        if(string == null) {
            return null;
        }
        String unescaped = StringEscapeUtils.unescapeHtml4(string);
        return unescaped;
    }


    @Override
    public UcumVersion getUCUMVersion() {
        return ucumVersion;
    }

    @Override
    public void setUCUMVersion(UcumVersion ucumVersion) {
        this.ucumVersion = ucumVersion;
    }
}
