package io.github.fhnaumann.converter;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.fhnaumann.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {

    private Converter converter;

    @BeforeEach
    public void setUp() {
        converter = new Converter();
    }

    @Test
    public void test_convert() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, inch_term()), meter_term());
        System.out.println(result);
        assert_cf(result, pd_u("0.0254"));
    }

    @Test
    public void inch_to_cm() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, inch_term()), cm_term());
        assert_cf(result, pd_u("2.54"));
    }

    @Test
    public void inch2_to_cm2() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, inch2_term()), cm2_term());
        assert_cf(result, pd_u("6.4516"));
    }

    @Test
    public void mega_inch_to_cm() {
        UCUMExpression.Term mega_inch = SoloTermBuilder.builder().withPrefix(mega, inches).noExpNoAnnot().asTerm().build();
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, mega_inch), cm_term());
        assert_cf(result, pd_u("2540000"));
    }

    @Test
    public void mega_inch2_to_dm2() {
        UCUMExpression.Term inch2 = SoloTermBuilder.builder().withPrefix(mega, inches).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        UCUMExpression.Term meter2 = SoloTermBuilder.builder().withPrefix(dezi, meter).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, inch2), meter2);
        assert_cf(result, pd_u("64516000000"));
    }

    /*
    @ParameterizedTest
    @MethodSource("provideConverts")
    public void test_multiple_convert(CTestCase cTestCase) {
        Converter.ConversionResult result = Converter._convert_debug(cTestCase.factor, cTestCase.fromSuccess, cTestCase.toSuccess);
        assertThat(result)
            .isInstanceOf(Success.class)
            .extracting(Success.class::cast)
            .satisfies(success -> assertThat(success.conversionFactor().toString()).startsWith(cTestCase.expectedCf))    }

    private static Stream<CTestCase> provideConverts() {
        return Stream.of(
            c_test_case("m->[in_i]", "39.3700", "1", "1", "1", null, "1", "0.0254", null),
            c_test_case("[in_i]->m", "0.0254", "1", "1", "0.0254", null, "1", "1", null),
            c_test_case("Cel->K", "274.15", "1", "1", "1", SpecialUnits.getFunction("Cel"), "1", "1", null),
            c_test_case("degF->K", "274.15", "1", "1", "1", SpecialUnits.getFunction("Cel"), "1", "1", null),
            c_test_case("B[SPL]->g.m-1.s-2", "0.0632", "1", "1", "0.02", SpecialUnits.getFunction("lgTimes2"), "1", "1", null),
            c_test_case("cf3: 5.B[SPL]->g.m-1.s-2", "0.02377", "3", "0.05", "0.02", SpecialUnits.getFunction("lgTimes2"), "1", "1", null)
        );
    }

    private static CTestCase c_test_case(String display, String expectedCf, String factor, String fromcfPrefix, String fromMagnitude, SpecialUnitsFunctionProvider.ConversionFunction fromSpecialConvFunc, String tocfPrefix, String toMagnitude, SpecialUnitsFunctionProvider.ConversionFunction toSpecialConvFunc) {
        PreciseDecimal fromcfPrefixPd = pd_u(fromcfPrefix);
        PreciseDecimal fromMagnitudePd = pd_u(fromMagnitude);
        PreciseDecimal tocfPrefixPd = pd_u(tocfPrefix);
        PreciseDecimal toMagnitudePd = pd_u(toMagnitude);
        Success2 fromSuccess;
        if(fromSpecialConvFunc == null) {
            fromSuccess = new SuccessNoSpecialUnit(fromcfPrefixPd, fromMagnitudePd);
        }
        else {
            fromSuccess = new SuccessSpecialUnit(fromcfPrefixPd, fromMagnitudePd, fromSpecialConvFunc);
        }
        Success2 toSuccess;
        if(toSpecialConvFunc == null) {
            toSuccess = new SuccessNoSpecialUnit(tocfPrefixPd, toMagnitudePd);
        }
        else {
            toSuccess = new SuccessSpecialUnit(tocfPrefixPd, toMagnitudePd, toSpecialConvFunc);
        }
        return new CTestCase(display, expectedCf, pd_u(factor), fromSuccess, toSuccess);
    }

    public record CTestCase(String display, String expectedCf, PreciseDecimal factor, Success2 fromSuccess, Success2 toSuccess) {
        @Override public String toString() {
            return display;
        }
    }

     */

    private static void assert_cf(Converter.ConversionResult actual, PreciseDecimal expectedCf) {
        assertThat(actual)
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast)
                .extracting(Converter.Success::conversionFactor)
                .isEqualTo(expectedCf);
    }
}
