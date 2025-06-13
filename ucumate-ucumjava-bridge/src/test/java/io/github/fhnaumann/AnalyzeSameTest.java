package io.github.fhnaumann;

import org.fhir.ucum.UcumException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class AnalyzeSameTest extends UcumateToUcumJavaTestBase {


    @ParameterizedTest
    @MethodSource("provide_units")
    public void test_they_print_same(String unit) throws UcumException {
        String expected = oldService.analyse(unit);
        String newActual = newService.analyse(unit);
        assertThat(newActual).isEqualTo(expected);
    }
}
