package org.example.validator;

import org.example.funcs.PrettyPrinter;
import org.example.funcs.Validator;
import org.example.funcs.printer.PrettyPrinter2;
import org.example.funcs.printer.UCUMSyntaxPrinter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.example.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidatorTest {

    @ParameterizedTest
    @DisplayName("Validation works as expected")
    @MethodSource("provide_validator_input_strings")
    public void test(VTestCase vTestCase) {
        Validator.ValidationResult result = Validator.validate(vTestCase.actual());
        assertThat(result)
                .isInstanceOf(Validator.Success.class)
                .extracting(Validator.Success.class::cast)
                .extracting(Validator.Success::term)
                .extracting(term -> new UCUMSyntaxPrinter().print(term))
                .isEqualTo(vTestCase.expectedDisplay());
    }

    @Test
    public void test2() {
        Validator.ValidationResult result = Validator.validate("g{正}");
        System.out.println(result);
    }

    private static Stream<VTestCase> provide_validator_input_strings() {
        return Stream.of(
                test("m", "m"),
                test("m/s", "m/s"),
                test("m/(s.g)", "m/(s.g)")
        );
    }

    public record VTestCase(String actual, String expectedDisplay) {
        @Override
        public String toString() {
            return String.format("%s -> %s", actual, expectedDisplay);
        }
    }

    private static VTestCase test(String actual, String expectedDisplay) {
        return new VTestCase(actual, expectedDisplay);
    }
}
