package io.github.fhnaumann;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.configuration.InputLimits;
import io.github.fhnaumann.funcs.CanonicalizerService;
import io.github.fhnaumann.funcs.ConverterService;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.util.PreciseDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Inputs that previously caused unbounded computation time or a StackOverflowError (see #83).
 *
 * @author Felix Naumann
 */
public class TestInputLimits {

    private static final Duration FAST = Duration.ofSeconds(2);

    private UCUMService service;

    @BeforeEach
    public void init() {
        ConfigurationRegistry.initialize(null);
        service = new UCUMService();
    }

    @AfterEach
    public void reset() {
        ConfigurationRegistry.initialize(null);
    }

    @ParameterizedTest
    @MethodSource("provide_expensive_conversions")
    public void expensive_conversion_is_rejected(String value, String from, String to) {
        ConverterService.ConversionResult result = assertTimeoutPreemptively(FAST, () -> service.convert(value, from, to));
        assertThat(result).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    @ParameterizedTest
    @MethodSource("provide_expensive_conversions")
    public void expensive_canonicalization_is_rejected(String value, String from, String to) {
        CanonicalizerService.CanonicalizationResult result = assertTimeoutPreemptively(FAST, () -> service.canonicalize(value, from));
        assertThat(result).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    private static Stream<Arguments> provide_expensive_conversions() {
        return Stream.of(
                Arguments.of("5", "10*999999.g", "mg"),
                Arguments.of("5", "[pi]99999.g", "g"),
                Arguments.of("5e1000000", "g", "mg"),
                Arguments.of("1".repeat(100_000), "g", "mg"),
                Arguments.of("5", "(".repeat(5000) + "m" + ")".repeat(5000), "m"),
                Arguments.of("5", "m" + ".m".repeat(20_000), "m"),
                // curly braces within square brackets are not an annotation and must be counted
                Arguments.of("5", "[{]" + ".m".repeat(5000), "m")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"10*999999.g", "[pi]99999.g", "m301", "m150.s-151", "m2147483647"})
    public void validation_agrees_with_conversion(String unit) {
        assertThat(service.validate(unit)).isInstanceOf(ValidatorService.Failure.class);
        assertThat(service.convert(unit, unit)).isInstanceOf(ValidatorService.InputLimitExceeded.class);
        assertThat(service.checkCommensurable(unit, unit)).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"m300", "m150.s-150", "4.[pi].10*-7.N/A2", "mL/min/(173.10*-2.m2)", "10*-30.g"})
    public void realistic_units_are_accepted(String unit) {
        assertThat(service.validateToBool(unit)).isTrue();
        assertThat(service.canonicalize(unit)).isInstanceOf(CanonicalizerService.Success.class);
    }

    @Test
    public void annotation_text_does_not_count_towards_the_length() {
        String unit = "m{" + "x".repeat(5000) + "}";
        assertThat(service.convert(unit, "cm")).isInstanceOf(ConverterService.Success.class);
    }

    @Test
    public void value_digit_limit_is_inclusive() {
        assertThat(service.convert(new PreciseDecimal("1e999"), "g", "g")).isInstanceOf(ConverterService.Success.class);
        assertThat(service.convert(new PreciseDecimal("1e1000"), "g", "g")).isInstanceOf(ValidatorService.InputLimitExceeded.class);
        assertThat(service.convert(new PreciseDecimal("1e-1000"), "g", "g")).isInstanceOf(ConverterService.Success.class);
        assertThat(service.convert(new PreciseDecimal("1e-1001"), "g", "g")).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    @Test
    public void already_parsed_terms_are_checked() {
        UCUMService unlimited = new UCUMService(UcumVersion.V2_2, InputLimits.unlimited());
        UCUMExpression.Term term = unlimited.parseOrError("[pi]99999");
        assertThat(service.canonicalize(term)).isInstanceOf(ValidatorService.InputLimitExceeded.class);
        assertThat(service.checkRelation(term, term)).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    @Test
    public void deep_terms_are_rejected_without_overflowing() {
        UCUMExpression.Term term = service.parseOrError("m");
        for(int i = 0; i < 100_000; i++) {
            term = new UCUMExpression.MixedParenTerm(term);
        }
        assertThat(service.canonicalize(term)).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    @Test
    public void limits_can_be_disabled_per_instance() {
        UCUMService unlimited = new UCUMService(UcumVersion.V2_2, InputLimits.unlimited());
        assertThat(unlimited.validateToBool("[pi]301")).isTrue();
        assertThat(unlimited.canonicalize("[pi]301")).isInstanceOf(CanonicalizerService.Success.class);
        assertThat(unlimited.convert("1e1000", "g", "g")).isInstanceOf(ConverterService.Success.class);
    }

    @Test
    public void limits_can_be_disabled_globally() {
        ConfigurationRegistry.initialize(Configuration.builder().inputLimits(InputLimits.unlimited()).build());
        UCUMService globallyUnlimited = new UCUMService();
        assertThat(globallyUnlimited.canonicalize("[pi]301")).isInstanceOf(CanonicalizerService.Success.class);
    }

    @Test
    public void instance_limits_override_global_limits() {
        ConfigurationRegistry.initialize(Configuration.builder().inputLimits(InputLimits.unlimited()).build());
        UCUMService strict = new UCUMService(UcumVersion.V2_2, new InputLimits(true, 20, 5, 10));
        assertThat(strict.canonicalize("m5")).isInstanceOf(CanonicalizerService.Success.class);
        assertThat(strict.canonicalize("m6")).isInstanceOf(ValidatorService.InputLimitExceeded.class);
        assertThat(strict.canonicalize("m{" + "x".repeat(50) + "}.s")).isInstanceOf(CanonicalizerService.Success.class);
        assertThat(strict.validateToBool("m" + ".m".repeat(10))).isFalse();
        assertThat(strict.convert("12345678901", "m", "m")).isInstanceOf(ValidatorService.InputLimitExceeded.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"m99999999999", "99999999999", "10*99999999999", "m-99999999999"})
    public void numbers_overflowing_int_are_invalid_instead_of_throwing(String unit) {
        assertThat(service.validate(unit)).isInstanceOf(ValidatorService.Failure.class);
        UCUMService unlimited = new UCUMService(UcumVersion.V2_2, InputLimits.unlimited());
        assertThat(unlimited.validate(unit)).isInstanceOf(ValidatorService.Failure.class);
        assertThat(unlimited.convert(unit, "m")).isInstanceOf(ValidatorService.ParserError.class);
    }
}
