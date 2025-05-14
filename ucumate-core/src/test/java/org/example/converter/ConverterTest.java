package org.example.converter;

import org.example.builders.SoloTermBuilder;
import org.example.funcs.Converter;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.example.TestUtil.*;
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
        Expression.Term mega_inch = SoloTermBuilder.builder().withPrefix(mega, inches).noExpNoAnnot().asTerm().build();
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, mega_inch), cm_term());
        assert_cf(result, pd_u("2540000"));
    }

    @Test
    public void mega_inch2_to_dm2() {
        Expression.Term inch2 = SoloTermBuilder.builder().withPrefix(mega, inches).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        Expression.Term meter2 = SoloTermBuilder.builder().withPrefix(dezi, meter).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, inch2), meter2);
        assert_cf(result, pd_u("64516000000"));
    }

    private static void assert_cf(Converter.ConversionResult actual, PreciseDecimal expectedCf) {
        assertThat(actual)
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast)
                .extracting(Converter.Success::conversionFactor)
                .isEqualTo(expectedCf);
    }
}
