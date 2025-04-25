package org.example.special;

import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.example.special.SpecialUtil.*;
import static org.example.TestUtil.*;
import static org.example.CanonicalizerUtil.*;
import static org.assertj.core.api.Assertions.*;

public class CanonicalizerSpecialTest {

    private Canonicalizer canonicalizer;

    @BeforeEach
    public void setUp() {
        canonicalizer = new Canonicalizer();
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(celsius_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        System.out.println(result);
        assert_success(result, pd("274.1500"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf5_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(celsius_term(), new Canonicalizer.SpecialUnitConversionContext(FIVE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("278.1500"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_5_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_celsius_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("278.1500"), kelvin_term());
    }

    @Test
    public void canonicalize_cf5_5_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_celsius_term(), new Canonicalizer.SpecialUnitConversionContext(FIVE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("298.1500"), kelvin_term());
    }

    @Test
    public void canonicalize_cf1_Mega_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(mega_celsius_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("1000273.1500"), kelvin_term());
    }

    @Test
    public void canonicalize_cf1_5_Mega_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_mega_celsius_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("5000273.1500"), kelvin_term());
    }

    @Test
    public void canonicalize_cf3_5_Mega_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_mega_celsius_term(), new Canonicalizer.SpecialUnitConversionContext(THREE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("15000273.1500"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_degF() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(degF_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("255.9278"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_5_degF() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_degF_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("258.1500"), kelvin_term()); // todo I should handle proper rounding and precision project-wide soon...
    }

    @Test
    public void canonicalize_cf1_prism_diopter() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(prism_diop_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("0.0100"), rad_term());
    }

    @Test
    public void canonicalize_cf1_5_prism_diopter() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_prism_diop_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("0.0500"), rad_term());
    }

    @Test
    public void canonicalize_cf1_percent_slope() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(percentage_slope_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        assert_success(result, pd("0.000174527107784301"), rad_term());
    }
}
