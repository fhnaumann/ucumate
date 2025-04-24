package org.example.special;

import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.funcs.Converter;
import org.example.model.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.example.special.SpecialUtil.*;
import static org.example.TestUtil.*;
import static org.assertj.core.api.Assertions.*;

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
        assert_success_and_equal_to(result, pd("274.1500"));
    }

    @Test
    @DisplayName("cf5: Cel = 278.15 K")
    public void convert_cf5_celsius_to_kelvin() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(FIVE, celsius_term()), kelvin_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd("278.1500"));
    }

    @Test
    @DisplayName("cf1: 5.Cel = 278.15 K")
    public void convert_5_celsius_to_kelvin() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, _5_celsius_term()), kelvin_term());
        System.out.println(result);
        assert_success_and_equal_to(result, pd("278.1500"));
    }

    @Test
    @DisplayName("cf1: K = -272.15 Cel")
    public void convert_cf1_kelvin_to_celsius() {

    }


}
