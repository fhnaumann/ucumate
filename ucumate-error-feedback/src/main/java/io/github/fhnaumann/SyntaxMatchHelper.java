package io.github.fhnaumann;

import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.ParseUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    /*
    The key can be:
    - The case-insensitive code
    - The name instead of the code
    - The print symbol instead of the code
    - A stigmatized unit without the square brackets
     */
    private static final Map<String, Set<UCUMDefinition.UCUMUnit>> WRONG_UNIT_CODES_TO_CORRECT_UNIT_CODES = createWrongToCorrectUnitCodes();

    private static Map<String, Set<UCUMDefinition.UCUMUnit>> createWrongToCorrectUnitCodes() {
        return UCUMRegistry.getInstance().getAll().stream()
                .filter(UCUMDefinition.UCUMUnit.class::isInstance)
                .map(UCUMDefinition.UCUMUnit.class::cast)
                .flatMap(unit -> generateVariants(unit).stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new HashSet<>(Set.of(entry.getValue())),
                         (set1, set2) -> {
                            set1.addAll(set2);
                            return set1;
                         }
                ));
    }

    private static List<Map.Entry<String, UCUMDefinition.UCUMUnit>> generateVariants(UCUMDefinition.UCUMUnit unit) {
        return Stream.concat(
                Stream.of(
                        unit.codeCaseInsensitive(),
                        unit.printSymbol(),
                        (unit.code().contains("[") && unit.code().contains("]")) ? unit.code().replace("[", "").replace("]", "") : null
                ),
                        unit.names().stream())
                .filter(Objects::nonNull)
                .map(variant -> Map.entry(variant, unit))
                .toList();
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

        UCUMDefinition.UCUMPrefix matchedPrefix = null;
        int matchedPrefixIdx = -1;
        for(int i=1; i<invalidInput.length(); i++) {
            String potentialPrefix = invalidInput.substring(0, i);
            Optional<UCUMDefinition.UCUMPrefix> optionalUCUMPrefix = registry.getPrefix(potentialPrefix);
            if(optionalUCUMPrefix.isPresent()) {
                matchedPrefixIdx = i;
                matchedPrefix = optionalUCUMPrefix.get();
            }
        }

        for(int i=1; i<invalidInput.length(); i++) {
            String potentialUnit = invalidInput.substring(0, i);
            Optional<UCUMDefinition.UCUMUnit> optionalUCUMUnit = registry.getUCUMUnit(potentialUnit);
            if(optionalUCUMUnit.isPresent()) {

            }
        }

        return null;
    }

    private static String lookForPrefix() {
        return null;
    }
}
