package io.github.fhnaumann;

import io.github.fhnaumann.funcs.Lookup;
import io.github.fhnaumann.funcs.LookupService;
import io.github.fhnaumann.funcs.printer.UCUMSyntaxPrinter;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Felix Naumann
 */
public class LookupTest {

    private static LookupService lookupService;
    private static UCUMSyntaxPrinter printer;

    @BeforeAll
    public static void init() {
        lookupService = new Lookup();
        printer = new UCUMSyntaxPrinter();
    }

    @ParameterizedTest
    @MethodSource("provide_arguments")
    public void test_lookup(String input, boolean anyMatch, boolean directMatch, List<String> expectedUnitMatches) {
        LookupService.LookupResult lookupResult = lookupService.lookup(input);
        if(!anyMatch) {
            assertThat(lookupResult)
                    .isInstanceOf(LookupService.NoMatch.class);
        }
        else if(directMatch) {
           assertThat(lookupResult)
                   .isInstanceOf(LookupService.DirectMatch.class)
                   .extracting(LookupService.DirectMatch.class::cast)
                   .extracting(LookupService.DirectMatch::unit)
                   .extracting(printer::print)
                   .isEqualTo(expectedUnitMatches.getFirst());
        }
        else {
            assertThat(lookupResult)
                    .isInstanceOf(LookupService.MultipleMatches.class)
                    .extracting(LookupService.MultipleMatches.class::cast)
                    .extracting(LookupService.MultipleMatches::units)
                    .extracting(ucumUnits -> ucumUnits.stream().map(printer::print).toList())
                    .asInstanceOf(InstanceOfAssertFactories.LIST)
                    .containsExactlyInAnyOrderElementsOf(expectedUnitMatches);
        }
    }

    private static Stream<Arguments> provide_arguments() {
        return Stream.of(
                arguments("m", true, true, List.of("m")),
                arguments("g", true, true, List.of("g")),
                arguments("GS", true, false, List.of("G")),
                arguments("magnetic flux density", true, false, List.of("G", "T")),
                arguments("Wb/m2", true, false, List.of("T")),
                arguments("meter", true, false, List.of("m", "m[H2O]", "m[Hg]", "[BAU]", "[m/s2/Hz^(1/2)]")),
                arguments("°C", true, false, List.of("Cel", "cal_[15]", "cal_[20]")),
                arguments("&#176;C", true, false, List.of("Cel", "cal_[15]", "cal_[20]")),
                arguments("&deg;C", true, false, List.of("Cel", "cal_[15]", "cal_[20]")),
                arguments("a<sub>t</sub>", false, false, List.of()), // searching with tags is not supported
                arguments("ft_i", true, false, List.of("[ft_i]")),
                arguments("Q", true, false, List.of("C", "[hp_Q]", "[kp_Q]", "[hp'_Q]", "[gal_us]")),
                arguments("gfdbdfbdfb", false, false, List.of())
        );
    }
}
