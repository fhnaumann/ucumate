package io.github.fhnaumann;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.UCUMService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class TestFeatureFlagAnnotAfterParens {

    @ParameterizedTest
    @MethodSource("provide_expressions")
    public void test(String expression, boolean allowAnnotAfterParens, boolean expectedValid) {
        ConfigurationRegistry.initialize(Configuration.builder().allowAnnotAfterParens(allowAnnotAfterParens).build());

        boolean actualValid = UCUMService.validateToBool(expression);

        assertThat(actualValid).isEqualTo(expectedValid);
    }

    private static Stream<Arguments> provide_expressions() {
        return Stream.of(
                Arguments.of("m", true, true),
                Arguments.of("m", false, true),
                Arguments.of("(m)", true, true),
                Arguments.of("(m)", false, true),
                Arguments.of("(m.s)", true, true),
                Arguments.of("(m.s)", false, true),
                Arguments.of("m{abc}", true, true),
                Arguments.of("m{abc}", false, true),
                Arguments.of("(m.s){abc}", true, true),
                Arguments.of("(m.s){abc}", false, false),
                Arguments.of("((m.s)){abc}", true, true),
                Arguments.of("((m.s)){abc}", false, false),
                Arguments.of("((m.s)){abc}/3", true, true),
                Arguments.of("((m.s)){abc}/3", false, false)
        );
    }
}
