package io.github.fhnaumann;

import io.github.fhnaumann.funcs.PrinterService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.ExpressiveUCUMSyntaxPrinter;
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
public class PrinterTest {

    private static PrinterService printerService;

    @BeforeAll
    public static void init() {
        printerService = new ExpressiveUCUMSyntaxPrinter();
    }

    @Test
    public void delete() {
        ValidatorService.ValidationResult result = new Validator().validate("mL/({h'b}.m2)");
        System.out.println(result);
    }

    @ParameterizedTest
    @MethodSource("provide_expr")
    public void test(String input, String expected) {
        String actual = printerService.print(input);
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provide_expr() {
        return Stream.of(
                Arguments.of("mL/({h'b}.m2)", "milliliter / h'b * (meter ^ 2)")
        );
    }


}
