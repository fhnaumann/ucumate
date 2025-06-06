package me.fhnau.org.special;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.Canonicalizer.Success;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static me.fhnau.org.CanonicalizerUtil.*;
import static me.fhnau.org.TestUtil.*;
import static me.fhnau.org.special.SpecialUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CanonicalizerSpecialTest {

    private Canonicalizer canonicalizer;

    @BeforeEach
    public void setUp() {
        canonicalizer = new Canonicalizer();
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(celsius_term());
        System.out.println(result);
        assert_success(result, pd_u("274.15"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf5_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(celsius_term());
        assert_success(result, pd_u("278.15"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_5_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_celsius_term());
        assert_success(result, pd_u("278.15"), kelvin_term());
    }

    @Test
    public void canonicalize_cf5_5_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_celsius_term());
        assert_success(result, pd_u("298.15"), kelvin_term());
    }

    @Test
    public void canonicalize_cf1_Mega_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(mega_celsius_term());
        assert_success(result, pd_u("1000273.15"), kelvin_term());
    }

    @Test
    public void canonicalize_cf1_5_Mega_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_mega_celsius_term());
        assert_success(result, pd_u("5000273.15"), kelvin_term());
    }

    @Test
    public void canonicalize_cf3_5_Mega_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_mega_celsius_term());
        assert_success(result, pd_u("15000273.15"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_degF() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(degF_term());
        assert_success(result, pd_l("255.9278"), kelvin_term());
    }

    @Test
    @DisplayName("")
    public void canonicalize_cf1_5_degF() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_degF_term());
        assert_success(result, pd_l("258.1500"), kelvin_term());
    }

    @Test
    public void canonicalize_cf1_prism_diopter() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(prism_diop_term());
        assert_success(result, pd_l("0.0100"), rad_term());
    }

    @Test
    public void canonicalize_cf1_5_prism_diopter() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(_5_prism_diop_term());
        assert_success(result, pd_l("0.0500"), rad_term());
    }

    @Test
    public void canonicalize_cf1_percent_slope() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(percentage_slope_term());
        assert_success(result, pd_l("0.000175"), rad_term());
    }

    @Test
    public void canonicalize_cf1_5_percent_slope() {

    }

    @Test
    public void canonicalize_cf5_5_percent_slope() {

    }

   @Test
   public void canonicalize_cf1_5_kelvin() {
        var result = (Success) canonicalizer.canonicalize(_5_kelvin_term());

        assertThat(print(result.canonicalTerm())).isEqualTo("K1");
   }

    @Test
    public void canonicalize_cf3_5_kelvin() {
        var result = (Success) canonicalizer.canonicalize(_5_kelvin_term());
        assertThat(print(result.canonicalTerm())).isEqualTo("K1");
    }

}
