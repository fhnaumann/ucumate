package io.github.fhnaumann.util;

import io.github.fhnaumann.funcs.ValidatorService.ParserException;
import io.github.fhnaumann.model.UCUMDefinition;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParseUtil {

    public static void checkASCIIRangeForAnnotation(String rawAnnotation) {
        boolean hasInvalidAscii = !rawAnnotation.isEmpty() &&
                rawAnnotation.chars().anyMatch(annotChar ->
                        annotChar < 33 || annotChar > 126
                );

        boolean hasInvalidBrackets = !rawAnnotation.isEmpty() &&
                rawAnnotation.chars().anyMatch(annotChar ->
                        annotChar == '{' || annotChar == '}'
                );

        boolean illegal = hasInvalidAscii || hasInvalidBrackets;
        if(hasInvalidAscii) {
            throw new ParserException("Invalid ASCII symbol or in annotation.");
        }
        if(illegal) {
            throw new ParserException("nesting");
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

    private static MatchResult computeMatchResult(String textMaybeWithUCUMUnit, String partialText, IUCUMRegistry registry) {
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

    public static MatchResult separatePrefixFromUnit(String textMaybeWithUCUMUnit, IUCUMRegistry registry) {
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
                .filter(SuccessResult.class::isInstance)
                .min(preferUnitsOverPrefixedUnits())
                .orElse(new InvalidResults(matchResults.stream()
                               .distinct()
                              .filter(FailureResult.class::isInstance)
                              .map(FailureResult.class::cast)
                              .toList())
                       );
    }

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
