package io.github.fhnaumann;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.persistence.PersistenceProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class TestFeatureFlagPrefixNonMetricUnits {

    @BeforeAll
    public static void init() {
        //System.setProperty("ucumate.cache.enable", "false");
    }

    @ParameterizedTest
    @MethodSource("provide_terms")
    public void test(String expression, boolean enablePrefixOnNonMetricUnits, boolean expectedValid) {

        Configuration config = Configuration.builder().enablePrefixOnNonMetricUnits(enablePrefixOnNonMetricUnits).build();
        ConfigurationRegistry.initialize(config);

        boolean actualValid = UCUMService.validateToBool(expression);
        assertThat(actualValid).isEqualTo(expectedValid);
    }

    private static Stream<Arguments> provide_terms() {
        return Stream.of(
                Arguments.of("m", true, true),
                Arguments.of("m", false, true),
                Arguments.of("cm", true, true),
                Arguments.of("cm", false, true),
                Arguments.of("mm", true, true),
                Arguments.of("mm", false, true),
                Arguments.of("c[ft_i]", false, false),
                Arguments.of("c[ft_i]", true, true),
                Arguments.of("da", false, false),
                Arguments.of("da", true, true),
                Arguments.of("nonsense", false, false),
                Arguments.of("nonsense", true, false)
        );
    }
}
