package io.github.fhnaumann.special;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.fhnaumann.TestUtil.*;
import static io.github.fhnaumann.special.SpecialUtil.*;

public class SpecialTest {

    private Converter converter;

    @BeforeEach
    public void setUp() {
        converter = new Converter();
    }

    @Test
    @DisplayName("cf1: Cel = 274.15 K")
    public void convert_cf1_celsius_to_kelvin() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, celsius_term()), kelvin_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("274.1500"));
    }

    @Test
    @DisplayName("cf5: Cel = 278.15 K")
    public void convert_cf5_celsius_to_kelvin() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(FIVE, celsius_term()), kelvin_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("278.1500"));
    }

    @Test
    @DisplayName("cf1: 5.Cel = 278.15 K")
    public void convert_5_celsius_to_kelvin() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, _5_celsius_term()), kelvin_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("278.1500"));
    }

    @Test
    @DisplayName("cf1: K = -272.15 Cel")
    public void convert_cf1_kelvin_to_celsius() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, kelvin_term()), celsius_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("-272.1500"));
    }

    @Test
    @DisplayName("cf1: K = -270.15 Cel")
    public void convert_cf3_kelvin_to_celsius() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(THREE, kelvin_term()), celsius_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("-270.1500"));
    }

    @Test
    @DisplayName("cf1: 5.K = -268.15 Cel")
    public void convert_cf1_5_kelvin_to_celsius() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, _5_kelvin_term()), celsius_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("-268.1500"));
    }

    @Test
    @DisplayName("cf1: K = -??? Cel")
    public void convert_cf3_5_kelvin_to_celsius() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(THREE, _5_kelvin_term()), celsius_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("-258.1500"));
    }

    @Test
    public void convert_cf49_5_BSPL_to() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(new PreciseDecimal("3"), parse("cB[SPL].5")), parse("g.m-1.s-2"));
        System.out.println(result);
        assert_success_and_equal_to(result, pd_l("0.02377"));
    }

    public void delete_me_4() {
        //Converter.ConversionResult result = UCUMService.convert(new PreciseDecimal("49"), , );
        //System.out.println(result);
        Converter.ConversionResult result2 = UCUMService.convert(new PreciseDecimal("49"), parse("5.cm-1.s-2.mg1.3"), parse("mB[SPL]"));
        System.out.println(result2);
    }





}
