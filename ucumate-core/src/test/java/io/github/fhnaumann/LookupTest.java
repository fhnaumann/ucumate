package io.github.fhnaumann;

import io.github.fhnaumann.funcs.Lookup;
import io.github.fhnaumann.funcs.LookupService;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.printer.UCUMSyntaxPrinter;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.util.Lists;
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
                    .isInstanceOf(LookupService.Failure.class);
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
                arguments("meter", true, false, List.of("m", "m[H2O]", "m[Hg]", "[BAU]", "[m/s2/Hz^(1/2)]"))
        );
    }
}
