package io.github.fhnaumann.canonicalizer;

import io.github.fhnaumann.configuration.CacheConfiguration;
import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CanonicalizerTest {

    private static Canonicalizer canonicalizer;
    private static Printer printer;

    static {
        // Must be set before ConfigurationRegistry/PersistenceRegistry are loaded. Disabling the cache also skips
        // the preheat, so the parser/visitor only runs for the actual test inputs.
        System.setProperty("ucumate.cache.enable", "false");
    }

    @BeforeAll
    public static void setUp() {
        printer = new Printer();
        canonicalizer = new Canonicalizer(printer, new Validator());
    }

    private static Stream<Arguments> provide_leading_slash_conversion() {
        return Stream.of(
                Arguments.of("1", "/m.s", "1", "m-1.s"),
                Arguments.of("1", "/m/s", "1", "m-1.s-1"),
                Arguments.of("1", "/(m.s)", "1", "m-1.s-1"),
                Arguments.of("1", "/(m/s)", "1", "m-1.s"),
                Arguments.of("1", "/m{a}", "1", "m-1"),
                Arguments.of("1", "/m.{a}", "1", "m-1"),
                Arguments.of("1", "/m/{a}", "1", "m-1"),
                Arguments.of("1", "/(m{a})", "1", "m-1"),
                Arguments.of("1", "/(m.{a})", "1", "m-1"),
                Arguments.of("1", "/{a}", "1", "1"),
                Arguments.of("1", "/({a})", "1", "1"),
                Arguments.of("1", "/({a}.m)", "1", "m-1"),
                Arguments.of("1", "/({a}).m", "1", "m"),
                Arguments.of("1", "/(m).s", "1", "m-1.s"),
                Arguments.of("1", "/m/s/g", "1", "g-1.m-1.s-1"),
                Arguments.of("1", "/m/s.g", "1", "g.m-1.s-1"),
                Arguments.of("1", "/min/10*3", "0.00001666666666666666666666666666666666666666666667", "s-1"),
                Arguments.of("1", "Oe", "79.5774715459476678844418816862571810172298228702275", "C.m-1.s-1"),
                Arguments.of("1", "Gb", "0.795774715459476678844418816862571810172298228702275", "C.s-1")
        );
    }

    @ParameterizedTest
    @MethodSource("provide_leading_slash_conversion")
    public void test_leading_slash_applies_to_limited_range_only(String factor, String from, String expectedFactor, String expectedTerm) {
        CanonicalizerService.CanonicalizationResult canonicalizationResult = canonicalizer.canonicalize(factor, from);
        assertThat(canonicalizationResult)
                .isInstanceOf(Canonicalizer.Success.class)
                .extracting(Canonicalizer.Success.class::cast)
                .matches(success -> success.magnitude().equals(new PreciseDecimal(expectedFactor)), "expected: " + expectedFactor)
                .extracting(CanonicalizerService.Success::canonicalTerm)
                .extracting(printer::print)
                .isEqualTo(expectedTerm);
    }

    @ParameterizedTest
    @ValueSource(strings = {"m./s", "m//s", "(/m)", "m.(/s)", "//m"})
    public void test_unary_div_only_allowed_at_start(String input) {
        assertThat(new Validator().validate(input)).isInstanceOf(ValidatorService.Failure.class);
    }
}
