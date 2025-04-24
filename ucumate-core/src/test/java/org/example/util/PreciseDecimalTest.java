package org.example.util;

import org.example.UCUMRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.example.TestUtil.*;
import static org.assertj.core.api.Assertions.*;

public class PreciseDecimalTest {

    @Test
    public void minimum_precision_is_kept_from_addition() {
        assertThat(pd("1.001").add(pd("2.00")))
                .satisfies(preciseDecimal -> {
                    assertThat(preciseDecimal.isLimited()).isTrue();
                    assertThat(preciseDecimal.getPrecision()).isEqualTo(3);
                    assertThat(preciseDecimal.getScale()).isEqualTo(2);
                });
        assertThat(pd("1.000000000001").add(pd("2.00")))
                .satisfies(preciseDecimal -> {
                    assertThat(preciseDecimal.isLimited()).isTrue();
                    assertThat(preciseDecimal.getPrecision()).isEqualTo(3);
                    assertThat(preciseDecimal.getScale()).isEqualTo(2);
                });
    }

    @Test
    public void pos_scientific_notation_without_decimal_is_not_limited() {
        assertThat(pd("1e2"))
                .satisfies(preciseDecimal -> {
                    assertThat(preciseDecimal.toString()).isEqualTo("100");
                    assertThat(preciseDecimal.isLimited()).isFalse();
                });
    }

    @Test
    public void neg_scientific_notation_without_decimal_is_not_limited() {
        assertThat(pd("1e-2"))
                .satisfies(preciseDecimal -> {
                    assertThat(preciseDecimal.toString()).isEqualTo("0.01");
                    assertThat(preciseDecimal.isLimited()).isFalse();
                });
    }

    @Test
    public void pos_scientific_notation_with_decimal_is_limited() {
        // Question: Should
        // 1.0e2 -> 100
        // 1.00e2 -> 100
        // 1.000e2 -> 100.0

        // 1.0e2 -> 1*100 + 0.0*100 -> 100.0
        // 1.0e3 -> 1*1000 + 0.0*1000 -> 1000.0
        // 1.0e1 -> 1.0*10 + 0.0*10 -> 10.0

        // 1.0e2 -> 100.0
        // 1.00e2 -> 100.00
        // 1.000e2 -> 100.000
        // Does 254e-2 -> 2.54 have infinite precision or 3 precision (scale 2)?
        assert_limited_with(pd("1.0e2"), "100.0", true, 2, 1);
        assert_limited_with(pd("1.00e2"), "100.00", true, 3, 2);
        assert_limited_with(pd("1.000e2"), "100.000", true, 4, 3);
        assert_limited_with(pd("1.0000e2"), "100.0000", true, 5, 4);
        assert_limited_with(pd("1.00000e2"), "100.00000", true, 6, 5);
    }

    @Test
    public void remove_me_later() {
        assertThat(pd("254e-2"))
                .satisfies(preciseDecimal -> {
                    assertThat(preciseDecimal.toString()).isEqualTo("2.54");
                    assertThat(preciseDecimal.isLimited()).isFalse();
                });
    }

    @Test
    public void pos_scientific_notation_with_multiple_digits_before_decimal_is_limited() {
        assert_limited_with(pd("2.54e2"), "254", true, 3, 0);
        assert_limited_with(pd("0.254e5"), "25400", true, 3, 0);
        assert_limited_with(pd("0.2540e5"), "25400", true, 4, 0);
        assert_limited_with(pd("0.25400e5"), "25400", true, 5, 0);
        assert_limited_with(pd("0.254000e5"), "25400.0", true, 6, 1);
        assert_limited_with(pd("0.2540000e5"), "25400.00", true, 7, 2);
        assert_limited_with(pd("0.25400000e5"), "25400.000", true, 8, 3);
        assert_limited_with(pd("0.254000001e5"), "25400.0001", true, 9, 4);
        assert_limited_with(pd("2.54e3"), "2540", true, 3, 0);
    }

    @Test
    public void neg_scientific_notation_with_multiple_digits_before_decimal_is_limited() {
        assert_limited_with(pd("2.54e-2"), "0.0254", true, 3, 4);
        assert_limited_with(pd("0.254e-5"), "0.00000254", true, 3, 8);
        assert_limited_with(pd("0.2540e-5"), "0.000002540", true, 4, 9);
        assert_limited_with(pd("0.25400e-5"), "0.0000025400", true, 5, 10);
    }

    @Test
    public void neg_scientific_notation_with_decimal_is_limited() {
        assert_limited_with(pd("1.0e-2"), "0.010", true, 2, 3);
        assert_limited_with(pd("1.00e-2"), "0.0100", true, 3, 4);
        assert_limited_with(pd("1.000e-2"), "0.01000", true, 4, 5);
        assert_limited_with(pd("1.0000e-2"), "0.010000", true, 5, 6);
        assert_limited_with(pd("1.00000e-2"), "0.0100000", true, 6, 7);
    }

    @Test
    public void pos_scientific_notation_with_neg_decimal_is_limited() {
        assert_limited_with(pd("-1.0e2"), "-100.0", true, 2, 1);
//        assert_limited_with(pd("1.00e2"), "-100.00", true, 3, 2);
//        assert_limited_with(pd("1.000e2"), "-100.000", true, 4, 3);
//        assert_limited_with(pd("1.0000e2"), "-100.0000", true, 5, 4);
//        assert_limited_with(pd("1.00000e2"), "-100.00000", true, 6, 5);
    }


    @Test
    public void prefix_range_is_parsed_correctly() {
        assertThat(pd("1e-24").toString()).isEqualTo("0.000000000000000000000001");
        assertThat(pd("1e-23").toString()).isEqualTo("0.00000000000000000000001");
        assertThat(pd("1e-22").toString()).isEqualTo("0.0000000000000000000001");
        assertThat(pd("1e-21").toString()).isEqualTo("0.000000000000000000001");
        assertThat(pd("1e-20").toString()).isEqualTo("0.00000000000000000001");
        assertThat(pd("1e-19").toString()).isEqualTo("0.0000000000000000001");
        assertThat(pd("1e-18").toString()).isEqualTo("0.000000000000000001");
        assertThat(pd("1e-17").toString()).isEqualTo("0.00000000000000001");
        assertThat(pd("1e-16").toString()).isEqualTo("0.0000000000000001");
        assertThat(pd("1e-15").toString()).isEqualTo("0.000000000000001");
        assertThat(pd("1e-14").toString()).isEqualTo("0.00000000000001");
        assertThat(pd("1e-13").toString()).isEqualTo("0.0000000000001");
        assertThat(pd("1e-12").toString()).isEqualTo("0.000000000001");
        assertThat(pd("1e-11").toString()).isEqualTo("0.00000000001");
        assertThat(pd("1e-10").toString()).isEqualTo("0.0000000001");
        assertThat(pd("1e-9").toString()).isEqualTo("0.000000001");
        assertThat(pd("1e-8").toString()).isEqualTo("0.00000001");
        assertThat(pd("1e-7").toString()).isEqualTo("0.0000001");
        assertThat(pd("1e-6").toString()).isEqualTo("0.000001");
        assertThat(pd("1e-5").toString()).isEqualTo("0.00001");
        assertThat(pd("1e-4").toString()).isEqualTo("0.0001");
        assertThat(pd("1e-3").toString()).isEqualTo("0.001");
        assertThat(pd("1e-2").toString()).isEqualTo("0.01");
        assertThat(pd("1e-1").toString()).isEqualTo("0.1");
        assertThat(pd("1e-0").toString()).isEqualTo("1");
        assertThat(pd("1e0").toString()).isEqualTo("1");
        assertThat(pd("1e1").toString()).isEqualTo("10");
        assertThat(pd("1e2").toString()).isEqualTo("100");
        assertThat(pd("1e3").toString()).isEqualTo("1000");
        assertThat(pd("1e4").toString()).isEqualTo("10000");
        assertThat(pd("1e5").toString()).isEqualTo("100000");
        assertThat(pd("1e6").toString()).isEqualTo("1000000");
        assertThat(pd("1e7").toString()).isEqualTo("10000000");
        assertThat(pd("1e8").toString()).isEqualTo("100000000");
        assertThat(pd("1e9").toString()).isEqualTo("1000000000");
        assertThat(pd("1e10").toString()).isEqualTo("10000000000");
        assertThat(pd("1e11").toString()).isEqualTo("100000000000");
        assertThat(pd("1e12").toString()).isEqualTo("1000000000000");
        assertThat(pd("1e13").toString()).isEqualTo("10000000000000");
        assertThat(pd("1e14").toString()).isEqualTo("100000000000000");
        assertThat(pd("1e15").toString()).isEqualTo("1000000000000000");
        assertThat(pd("1e16").toString()).isEqualTo("10000000000000000");
        assertThat(pd("1e17").toString()).isEqualTo("100000000000000000");
        assertThat(pd("1e18").toString()).isEqualTo("1000000000000000000");
        assertThat(pd("1e19").toString()).isEqualTo("10000000000000000000");
        assertThat(pd("1e20").toString()).isEqualTo("100000000000000000000");
        assertThat(pd("1e21").toString()).isEqualTo("1000000000000000000000");
        assertThat(pd("1e22").toString()).isEqualTo("10000000000000000000000");
        assertThat(pd("1e23").toString()).isEqualTo("100000000000000000000000");
        assertThat(pd("1e24").toString()).isEqualTo("1000000000000000000000000");
    }

    private static void assert_limited_with(PreciseDecimal actual, String expectedDisplay, boolean expectedLimited, int expectedPrecision, int expectedScale) {
        assertThat(actual.toString()).isEqualTo(expectedDisplay);
        assertThat(actual.isLimited()).isEqualTo(expectedLimited);
        assertThat(actual.getPrecision()).isEqualTo(expectedPrecision);
        assertThat(actual.getScale()).isEqualTo(expectedScale);
    }
}
