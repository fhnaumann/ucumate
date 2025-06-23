package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.PreciseDecimal;
import io.github.fhnaumann.util.UCUMRegistry;

import javax.swing.*;
import java.util.*;

/**
 * @author Felix Naumann
 */
public class Lookup implements LookupService {

    private static final UCUMRegistry registry = UCUMRegistry.getInstance();

    @Override
    public LookupResult lookup(String input) {
        Optional<UCUMDefinition.Concept> optDirectConceptMatch = registry.getConcept(input);
        if(optDirectConceptMatch.isPresent() && optDirectConceptMatch.get() instanceof UCUMDefinition.UCUMUnit unit) {
            return new DirectMatch(unit);
        }
        List<Map.Entry<UCUMDefinition.UCUMUnit, MatchType>> matches = findAndSortMatches(input);
        if(matches.isEmpty()) {
            return new Failure();
        }
        return new MultipleMatches(matches.stream().map(Map.Entry::getKey).toList());
    }

    private List<Map.Entry<UCUMDefinition.UCUMUnit, MatchType>> findAndSortMatches(String input) {
        List<Map.Entry<UCUMDefinition.UCUMUnit, MatchType>> matches = new ArrayList<>();
        for(UCUMDefinition.Concept concept : registry.getAll()) {
            if(!(concept instanceof UCUMDefinition.UCUMUnit unit)) {
                continue;
            }
            boolean nameDirect = false, printSymbolDirect = false, propertyDirect = false;
            if(unit.codeCaseInsensitive().equals(input)) {
                matches.add(Map.entry(unit, MatchType.CODE_CI));
            }
            if(unit.names().contains(input)) {
                nameDirect = true;
                matches.add(Map.entry(unit, MatchType.NAME_DIRECT));
            }
            if(!nameDirect && unit.names().stream().anyMatch(s -> s.contains(input))) {
                matches.add(Map.entry(unit, MatchType.NAME_CONTAINS));
            }
            if(input.equals(concept.printSymbol())) {
                printSymbolDirect = true;
                matches.add(Map.entry(unit, MatchType.PRINT_SYMBOL_DIRECT));
            }
            if(!printSymbolDirect && concept.printSymbol() != null && concept.printSymbol().contains(input)) {
                matches.add(Map.entry(unit, MatchType.PRINT_SYMBOL_CONTAINS));
            }
            if(input.equals(unit.property())) {
                propertyDirect = true;
                matches.add(Map.entry(unit, MatchType.PROPERTY_DIRECT));
            }
            if(!propertyDirect && unit.property() != null && unit.property().contains(input)) {
                matches.add(Map.entry(unit, MatchType.PROPERTY_CONTAINS));
            }
            if(unit instanceof UCUMDefinition.BaseUnit baseUnit && input.equals(baseUnit.dim())) {
                matches.add(Map.entry(unit, MatchType.BASE_DIM));
            }
            if(unit instanceof UCUMDefinition.DefinedUnit definedUnit && matchesSourcesDefinition(definedUnit.value(), input)) {
                matches.add(Map.entry(unit, MatchType.VALUE));
            }
        }
        matches.sort(Comparator.comparingInt(o -> o.getValue().getScore()));
        return matches;
    }

    private boolean matchesSourcesDefinition(UCUMDefinition.UCUMValue value, String input) {
        if(!PreciseDecimal.ONE.equals(value.conversionFactor())) {
            // cannot match any source definition that uses something else than 1 in its definition
            return false;
        }
        return input.equals(value.unit()) || input.equals(value.unitAlt());
    }


    private enum MatchType {
        CODE_CI(100),
        NAME_DIRECT(300),
        NAME_CONTAINS(350),
        PRINT_SYMBOL_DIRECT(200),
        PRINT_SYMBOL_CONTAINS(250),
        VALUE(400),
        PROPERTY_DIRECT(500),
        PROPERTY_CONTAINS(550),
        BASE_DIM(600);

        private final int score;

        MatchType(int score) {
            this.score = score;
        }

        public int getScore() {
            return score;
        }
    }
}
