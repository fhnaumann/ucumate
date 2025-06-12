package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.UCUMRegistry;
import org.fhir.ucum.UcumException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class PrintSameTest extends UcumateToUcumJavaTestBase {


    @ParameterizedTest
    @MethodSource("provide_prints")
    public void test_they_print_same(String unit) throws UcumException {
        String expected = oldService.analyse(unit);
        String newActual = newService.analyse(unit);
        assertThat(newActual).isEqualTo(expected);
    }

    @Test
    public void delete() {
        System.out.println(UCUMService.convert("g/L", "mol/L"));
    }

    private static Stream<String> provide_prints() {
        return Stream.of(
                "m",
                "cm",
                "m.s",
                "m/s",
                "[lton_av]",
                "cm2",
                "cm-2",
                "cm+0",
                "cm-0",
                "cm1",
                "[ft_i]",
                "2.[ft_i]/s",
                "(m.s).g",
                "m/s/g",
                "(m/s)/g",
                "m/(s/g)",
                "m/(s.g)",
                "m/s.g",
                "{}",
                "{abc}",
                "m{abc}",
                "cm{abc}",
                "(m{}.s{abc}).g{deb}",
                "(m2{}.s-3{abc}).g0{deb}",
                "/m",
                "/(m.s)"
        );
    }
}
