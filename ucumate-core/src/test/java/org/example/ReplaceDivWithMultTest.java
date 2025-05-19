package org.example;

import org.example.funcs.ReplaceDivWithMult;
import org.example.model.Expression;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtil.*;

public class ReplaceDivWithMultTest {

    @Test
    public void test_simple_division() {
        Expression.CanonicalTerm term = parse_canonical("m/s");
        Expression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1");
    }

    @Test
    public void test_nothing_changed_if_no_division_present() {
        Expression.CanonicalTerm term = parse_canonical("m.s.g");
        Expression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s1.g1");
    }

    @Test
    public void test_binary_mul_term_in_division() {
        Expression.CanonicalTerm term = parse_canonical("m/(s.g)");
        Expression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1.g-1");
    }

    @Test
    public void test_binary_div_term_in_division() {
        Expression.CanonicalTerm term = parse_canonical("m/(s/g)");
        Expression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1.g1");
    }

    @Test
    public void test_deep_binary_div_term_in_division() {
        Expression.CanonicalTerm term = parse_canonical("m/(s/(g/C))");
        Expression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1.g1.C-1");
    }
}
