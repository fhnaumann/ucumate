package io.github.fhnaumann.util;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.funcs.Validator.ParserException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParseUtil {

    public static void checkASCIIRangeForAnnotation(String rawAnnotation) {
        boolean illegalSymbols = !rawAnnotation.isEmpty() && rawAnnotation.chars().noneMatch(annotChar -> annotChar >= 33 && annotChar <= 126 && annotChar != '{' && annotChar != '}');
        if(illegalSymbols) {
            throw new ParserException("Invalid ascii symbol in annotation.");
        }
    }

    public static String asText(List<? extends ParseTree> nodes) {
        return nodes.stream().map(ParseTree::getText).collect(Collectors.joining());
    }

    public static boolean isMetric(UCUMDefinition.UCUMUnit ucumUnit) {
        return switch (ucumUnit) {
            case UCUMDefinition.BaseUnit baseUnit -> true; // base units are always metric
            case UCUMDefinition.DefinedUnit definedUnit -> definedUnit.isMetric();
        };
    }

    private static MatchResult computeMatchResult(String textMaybeWithUCUMUnit, String partialText, UCUMRegistry registry) {
        Optional<UCUMDefinition.UCUMUnit> optionalUCUMUnit = registry.getUCUMUnit(partialText);
        if(optionalUCUMUnit.isPresent()) {
            UCUMDefinition.UCUMUnit ucumUnit = optionalUCUMUnit.get();
            String remainingTextBeforeUCUMUnitMaybeContainingAPrefix = textMaybeWithUCUMUnit.substring(0,
                                                                                                       textMaybeWithUCUMUnit.length() - partialText.length()
            );
            if(remainingTextBeforeUCUMUnitMaybeContainingAPrefix.isEmpty()) {
                // the matching UCUM unit took up the entire string, there's nothing left to match
                return new SuccessNoPrefixUnit(ucumUnit);
            }
            Optional<UCUMDefinition.UCUMPrefix> optionalUCUMPrefix = registry.getPrefix(
                    remainingTextBeforeUCUMUnitMaybeContainingAPrefix);
            if(optionalUCUMPrefix.isPresent()) {
                UCUMDefinition.UCUMPrefix ucumPrefix = optionalUCUMPrefix.get();
                return new SuccessPrefixUnit(ucumPrefix, ucumUnit);
            } else {
                // there is SOMETHING before the matched unit, but it's not a valid prefix
                return new InvalidPrefix(remainingTextBeforeUCUMUnitMaybeContainingAPrefix);
            }
        } else {
            // couldn't match a unit, did not check the prefix because a unit HAS to be matched first
            return new InvalidUnit(textMaybeWithUCUMUnit);
        }
    }

    private static Comparator<MatchResult> preferUnitsOverPrefixedUnits() {
        return (o1, o2) -> {
            if (o1 instanceof SuccessPrefixUnit && o2 instanceof SuccessNoPrefixUnit) {
                return 1;
            }
            if (o1 instanceof SuccessNoPrefixUnit && o2 instanceof SuccessPrefixUnit) {
                return -1;
            } else {
                return 0;
            }
        };
    }

    public static MatchResult separatePrefixFromUnit(String textMaybeWithUCUMUnit, UCUMRegistry registry) {
        List<MatchResult> matchResults = IntStream.iterate(textMaybeWithUCUMUnit.length() - 1,
                                                           i -> i >= 0,
                                                           i -> i - 1
                                                  )
                                                  .mapToObj(textMaybeWithUCUMUnit::substring)
                                                  .map(string -> computeMatchResult(textMaybeWithUCUMUnit,
                                                                                    string,
                                                                                    registry
                                                  ))
                                                  .toList();
        return matchResults.stream()
                           .filter(matchResult -> matchResult instanceof SuccessResult)
                .sorted(preferUnitsOverPrefixedUnits())
                           .findFirst()
                           .orElse(new InvalidResults(matchResults.stream()
                                   .distinct()
                                                                  .filter(matchResult -> matchResult instanceof FailureResult)
                                                                  .map(FailureResult.class::cast)
                                                                  .toList())
                           );
    }

    // TODO Rework this when proper error handling/correction suggestion is implemented
    public sealed interface MatchResult {}

    sealed interface SuccessResult extends MatchResult {}

    public sealed interface FailureResult extends MatchResult {
        String failedText();
    }

    public record SuccessNoPrefixUnit(
            UCUMDefinition.UCUMUnit unit
    ) implements SuccessResult {}

    public record SuccessPrefixUnit(
            UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit
    ) implements SuccessResult {}

    public record InvalidPrefix(String failedText) implements FailureResult {}

    public record InvalidUnit(String failedText) implements FailureResult {}

    public record InvalidResults(List<FailureResult> failureResults) implements MatchResult {}
}
