package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.ParseUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class SyntaxMatchHelper {

    private static final Set<String> WRONG_BUT_KNOWN_MUL_SYMBOLS = Set.of(
            "*", // Asteriks
            "×", // U+00D7 MULTIPLICATION SIGN
            "✕", // U+2715 MULTIPLICATION X
            "✖", // U+2716 HEAVY MULTIPLICATION X
            "⨯" // U+2A2F VECTOR OR CROSS PRODUCT
    );

    private static final Set<String> WRONG_BUT_KNOWN_DIV_SYMBOLS = Set.of(
            "÷", // U+00F7 DIVISION SIGN
            "∕", // U+2215 DIVISION SLASH
            "➗" // U+2797 HEAVY DIVISION SIGN
    );

    private static final Set<String> WRONG_BUT_KNOWN_EXP_SYMBOLS = Set.of(
            "^", // caret
            "ʌ", // U+028C LATIN SMALL LETTER TURNED V
            "‸" // U+2038 CARET
    );

    private record Score(Set<UCUMDefinition.UCUMUnit> units, int score) {}

    /*
    The key can be:
    - The case-insensitive code
    - The name instead of the code
    - The print symbol instead of the code
    - A stigmatized unit without the square brackets
     */
    private static final Map<String, Score> WRONG_UNIT_CODES_TO_CORRECT_UNIT_CODES = createWrongToCorrectUnitCodes();

    private static Map<String, Score> createWrongToCorrectUnitCodes() {
        return UCUMRegistry.getInstance().getAll().stream()
                .filter(UCUMDefinition.UCUMUnit.class::isInstance)
                .map(UCUMDefinition.UCUMUnit.class::cast)
                .flatMap(unit -> generateVariants(unit).stream())
                .collect(Collectors.toMap(
                        stringEntryEntry -> stringEntryEntry.getKey(),
                        entry -> new Score(Set.of(entry.getValue().getKey()), entry.getValue().getValue()),
                         (score1, score2) -> {
                            Set<UCUMDefinition.UCUMUnit> mergedUnits = new HashSet<>();
                            mergedUnits.addAll(score1.units());
                            mergedUnits.addAll(score2.units());
                            return new Score(mergedUnits, Math.min(score1.score(), score2.score()));
                         }
                ));
    }

    private static List<Map.Entry<String, Map.Entry<UCUMDefinition.UCUMUnit, Integer>>> generateVariants(UCUMDefinition.UCUMUnit unit) {
        /*
        Each variant has a different score.
        For now, only show matches with the lowest score (multiple may have the same score).
         */
        List<Map.Entry<String, Map.Entry<UCUMDefinition.UCUMUnit, Integer>>> list = new ArrayList<>();
        list.add(Map.entry(unit.codeCaseInsensitive(), Map.entry(unit, 100)));
        if(unit.printSymbol() != null) {
            list.add(Map.entry(unit.printSymbol(), Map.entry(unit, 200)));
        }
        if(unit.code().contains("[") && unit.code().contains("]")) {
            String removedSquareBrackets = unit.code().replace("[", "").replace("]", "");
            list.add(Map.entry(removedSquareBrackets, Map.entry(unit, 50)));
        }
        unit.names().forEach(s -> list.add(Map.entry(s, Map.entry(unit, 300))));
        return list;
    }

    public static void checkWrongButKnownMulSymbolUsed(String mulSymbol, List<String> errorMessages) {
        if(WRONG_BUT_KNOWN_MUL_SYMBOLS.contains(mulSymbol)) {
            errorMessages.add(ErrorMessages.get("wrong_multiplication", mulSymbol));
        }
    }

    public static void checkWrongButKnownDivSymbolUsed(String divSymbol, List<String> errorMessages) {
        if(WRONG_BUT_KNOWN_DIV_SYMBOLS.contains(divSymbol)) {
            errorMessages.add(ErrorMessages.get("wrong_division", divSymbol));
        }
    }

    public static void checkWrongButKnownExpSymbolUsed(String expSymbol, List<String> errorMessages) {
        if(WRONG_BUT_KNOWN_EXP_SYMBOLS.contains(expSymbol)) {
            errorMessages.add(ErrorMessages.get("wrong_exponent", expSymbol));
        }
    }

    public static void checkWhiteSpace(CommonTokenStream tokens, List<String> errorMessages) {
        tokens.fill();
        boolean hasWhitespaces = tokens.getTokens().stream()
                .anyMatch(tok -> tok.getChannel() == Token.HIDDEN_CHANNEL &&
                        tok.getType() == ErrorFeedbackUCUMLexer.WS);
        if(hasWhitespaces) {
            errorMessages.add(ErrorMessages.get("spaces"));
        }
    }

    public static List<String> extractErrorMessagesFrom(Validator.ParserException parserException) {
        return parserException.getFailures().stream()
                .map(SyntaxMatchHelper::mapErrorTypeToErrorMessage)
                .toList();
    }

    private static String mapErrorTypeToErrorMessage(ParseUtil.FailureResult failureResult) {
        String rbString = switch (failureResult) {
            case ParseUtil.InvalidPrefix invalidPrefix -> "prefix_match_failure";
            case ParseUtil.InvalidUnit invalidUnit -> "unit_match_failure";
        };
        return ErrorMessages.get(rbString, failureResult.failedText());
    }

    public static String analyseUnitForErrorDetails(String invalidInput) {
        UCUMRegistry registry = UCUMRegistry.getInstance();
        // 1) look for prefix
        // 2) look for unit
        // 3) look for exponent
        // 4) look for annotation
        List<Map.Entry<UCUMDefinition.UCUMUnit, Integer>> matchedUnits = lookForUnits(invalidInput, registry);
        OptionalInt minScore = matchedUnits.stream().mapToInt(Map.Entry::getValue).min();
        if(minScore.isEmpty()) {
            return null;
        }
        List<UCUMDefinition.UCUMUnit> highestScoredUnits = matchedUnits.stream()
                .filter(ucumUnitIntegerEntry -> ucumUnitIntegerEntry.getValue() == minScore.getAsInt())
                .map(Map.Entry::getKey)
                .toList();
        //UCUMDefinition.UCUMPrefix matchedPrefix = lookForPrefix(invalidInput, registry);
        //int prefixOffset = matchedPrefix != null ? matchedPrefix.code().length() : 0;

        //String matchedPrefixPrint = matchedPrefix != null ? UCUMService.print(matchedPrefix) : "";
        String matchedUnitsString = highestScoredUnits.stream()
                .map(UCUMService::print)
                .collect(Collectors.joining(","));
        if(matchedUnits.isEmpty()) {
            // analyzing did not lead to anything meaningful, the input is truly indecipherable
            return null;
        }
        return ErrorMessages.get("unit_correction", invalidInput, matchedUnitsString);
    }

    private static List<Map.Entry<UCUMDefinition.UCUMUnit, Integer>> lookForUnits(String invalidInput, UCUMRegistry registry) {
        List<Map.Entry<UCUMDefinition.UCUMUnit, Integer>> list = new ArrayList<>();
        for(int i=invalidInput.length(); i>0; i--) {
            String potentialUnit = invalidInput.substring(0, i);
            Optional<UCUMDefinition.UCUMUnit> optDirectMatch = registry.getUCUMUnit(potentialUnit);
            if(optDirectMatch.isPresent()) {
                list.add(Map.entry(optDirectMatch.get(), 0)); // direct matches have the best score
                continue;
            }
            System.out.println(potentialUnit);
            Score wrongMatched = WRONG_UNIT_CODES_TO_CORRECT_UNIT_CODES.get(potentialUnit);
            if(wrongMatched != null) {
                wrongMatched.units().forEach(unit -> list.add(Map.entry(unit, wrongMatched.score())));
            }
        }
        return list.stream().distinct().sorted(Map.Entry.comparingByValue()).toList();
    }

    private static UCUMDefinition.UCUMPrefix lookForPrefix(String invalidInput, UCUMRegistry registry) {
        for(int i=1; i<invalidInput.length(); i++) {
            String potentialPrefix = invalidInput.substring(0, i);
            Optional<UCUMDefinition.UCUMPrefix> optionalUCUMPrefix = registry.getPrefix(potentialPrefix);
            if(optionalUCUMPrefix.isPresent()) {
                return optionalUCUMPrefix.get();
            }
        }
        return null;
    }

    public static void searchForAnyUnbalancedParens(TokenStream tokenStream, ParserRuleContext root, List<String> errorMessages) {
        MissingParenInfo roundParenInfo = extractUnbalancedParenSpan(tokenStream, root, ParenType.ROUND);
        MissingParenInfo squareParenInfo = extractUnbalancedParenSpan(tokenStream, root, ParenType.SQUARE);
        MissingParenInfo curlyParenInfo = extractUnbalancedParenSpan(tokenStream, root, ParenType.CURLY);

        reportParensError(roundParenInfo, "binary_term_missing_left_paren", "binary_term_missing_right_paren", errorMessages);
        reportParensError(squareParenInfo, "missing_left_square_bracket", "missing_right_square_bracket", errorMessages);
        reportParensError(curlyParenInfo, "missing_left_curly_bracket", "missing_right_curly_bracket", errorMessages);
    }

    private static void reportParensError(MissingParenInfo parenInfo, String leftMissingKey, String rightMissingKey, List<String> errorMessages) {
        if(parenInfo != null) {
            System.out.println(parenInfo.text());
            switch (parenInfo.side()) {
                case LEFT -> errorMessages.add(ErrorMessages.get(leftMissingKey, parenInfo.text()));
                case RIGHT -> errorMessages.add(ErrorMessages.get(rightMissingKey, parenInfo.text()));
            }
        }
    }

    private static MissingParenInfo extractUnbalancedParenSpan(TokenStream tokens, ParserRuleContext root, ParenType parenType) {
        int balance = 0;
        int unmatchedOpenIndex = -1;
        int unmatchedCloseIndex = -1;

        for (int i = root.getStart().getTokenIndex(); i <= root.getStop().getTokenIndex(); i++) {
            Token token = tokens.get(i);
            String text = token.getText();

            if (text.equals(parenType.open)) {
                if (balance == 0) unmatchedOpenIndex = token.getTokenIndex();
                balance++;
            } else if (text.equals(parenType.close)) {
                balance--;
                if (balance < 0) {
                    // Found unmatched closing
                    unmatchedCloseIndex = token.getTokenIndex();
                    break;
                }
                if (balance == 0) {
                    unmatchedOpenIndex = -1; // all matched again
                }
            }
        }

        if (unmatchedCloseIndex != -1) {
            Token end = tokens.get(unmatchedCloseIndex);
            Token start = tokens.get(root.getStart().getTokenIndex()); // go back one token
            return new MissingParenInfo(start.getInputStream().getText(Interval.of(start.getStartIndex(), end.getStopIndex())), MissingParenSide.LEFT);
        }

        if (unmatchedOpenIndex != -1) {
            Token start = tokens.get(unmatchedOpenIndex);
            Token end = tokens.get(root.getStop().getTokenIndex());
            return new MissingParenInfo(start.getInputStream().getText(Interval.of(start.getStartIndex(), end.getStopIndex())), MissingParenSide.RIGHT);
        }
        return null;
    }

    public record MissingParenInfo(String text, MissingParenSide side) {}

    public enum MissingParenSide {
        LEFT, RIGHT
    }

    public enum ParenType {
        ROUND("(", ")"),
        SQUARE("[", "]"),
        CURLY("{", "}");

        private final String open, close;

        ParenType(String open, String close) {
            this.open = open;
            this.close = close;
        }
    }

}
