package io.github.fhnaumann;

import org.fhir.ucum.UcumException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class GetCanonicalUnitsSameTest extends UcumateToUcumJavaTestBase {

    @ParameterizedTest
    @MethodSource("provide_units")
    public void test_get_canonical_units_return_same(String unit) throws UcumException {
        String expected = oldService.getCanonicalUnits(unit);
        String newActual = newService.getCanonicalUnits(unit);
        assertThat(newActual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provide_canonical_units")
    public void test_validate_canonical_units_returns_same(String unit, String canonical) {
        String expected = oldService.validateCanonicalUnits(unit, canonical);
        String newActual = newService.validateCanonicalUnits(unit, canonical);
        if(expected == null) {
            assertThat(newActual)
                    .withFailMessage("OldService returned null but NewServices returned %s.", newActual)
                    .isNull();
        }
        else {
            assertThat(newActual)
                    .withFailMessage("OldService returned %s but NewServices returned null.", expected)
                    .isNotNull()
                    .isNotBlank();
        }
    }

    private static Stream<Arguments> provide_canonical_units() {
        return Stream.of(
                Arguments.of("[ft_i]", "m"),
                Arguments.of("[ft_i]", "g"),
                Arguments.of("g", "g"),
                Arguments.of("5.g", "g"),
                Arguments.of("5.g", "gfsg"),
                Arguments.of("cm", "[ft_i]"),
                Arguments.of("[ft_i]", "[ft_i]")
        );
    }
}
