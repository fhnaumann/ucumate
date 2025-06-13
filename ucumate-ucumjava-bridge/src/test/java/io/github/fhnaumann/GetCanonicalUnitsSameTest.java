package io.github.fhnaumann;

import org.fhir.ucum.UcumException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
}
