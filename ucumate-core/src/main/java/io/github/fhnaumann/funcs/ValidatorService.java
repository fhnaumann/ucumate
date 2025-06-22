package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Felix Naumann
 */
public interface ValidatorService {

    /**
     * Validates a given string.
     * @param input A string containing a potential UCUMTerm.
     * @return A ValidationResult with information about the validity.
     *
     * @see UCUMService#validateToBool(String)
     * @see Validator.ValidationResult
     */
    public Validator.ValidationResult validate(String input);

    /**
     * Validate a given String and return a boolean.
     * @param input A string containing a potential UCUMTerm.
     * @return A boolean that was mapped from {@link Validator.ValidationResult} where {@link Validator.Success} -> true and {@link Validator.Failure} -> false.
     *
     * @see UCUMService#validate(String)
     */
    public default boolean validateToBool(String input) {
        return switch (validate(input)) {
            case Validator.Success success -> true;
            case Validator.Failure failure -> false;
        };
    }

    public sealed interface ValidationResult {}

    public record Success(UCUMExpression.Term term) implements ValidationResult {}

    public record Failure(String message) implements ValidationResult {
        public Failure() {
            this("");
        }
    }

    public class LexerException extends RuntimeException {
        public LexerException(String message) {
            super(message);
        }
    }

    public class ParserException extends RuntimeException {

        private final List<ParseUtil.FailureResult> failures;

        public ParserException(String message) {
            super(message);
            this.failures = new ArrayList<>();
        }
        public ParserException(ParseUtil.FailureResult failureResult) {
            super(failureResult.failedText());
            this.failures = List.of(failureResult);
        }
        public ParserException(ParseUtil.InvalidResults invalidResults) {
            super(invalidResults.toString());
            this.failures = invalidResults.failureResults();
        }

        public List<ParseUtil.FailureResult> getFailures() {
            return failures;
        }
    }

    public record ParserError() implements
            CanonicalizerService.FailedCanonicalization,
            Converter.FailedConversion, RelationChecker.FailedRelationCheck,
            RelationChecker.FailedCommensurableCheck
    {}
}
