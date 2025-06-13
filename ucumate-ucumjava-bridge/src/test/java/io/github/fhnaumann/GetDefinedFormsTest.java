package io.github.fhnaumann;

import org.fhir.ucum.DefinedUnit;
import org.fhir.ucum.UcumException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class GetDefinedFormsTest extends UcumateToUcumJavaTestBase {

    @ParameterizedTest
    @MethodSource("provide_units")
    public void test_get_defined_forms_returns_same(String unit) throws UcumException {
        List<DefinedUnit> expected = oldService.getDefinedForms(unit);
        List<DefinedUnit> newActual = newService.getDefinedForms(unit);
        assertThat(newActual.size()).isEqualTo(expected.size());
        assertThat(newActual.stream().map(DefinedUnit::getDescription).toList())
                .containsAll(expected.stream().map(DefinedUnit::getDescription).toList());
    }
}
