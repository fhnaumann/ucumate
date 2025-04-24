package org.example.converter;

import org.example.builders.SoloTermBuilder;
import org.example.funcs.Converter;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.example.TestUtil.*;
import static org.example.CanonicalizerUtil.*;
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
        assertThat(result)
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast)
                .extracting(Converter.Success::conversionFactor)
                .isEqualTo(pd("0.0254"));
    }

    @Test
    public void dummy() {
        Expression.Term inch2 = SoloTermBuilder.builder().withPrefix(mega, inches).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        Expression.Term meter2 = SoloTermBuilder.builder().withPrefix(dezi, getUCUMUnit("[ft_i]")).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, inch2), meter2);
        System.out.println(result);
    }
}
