package me.fhnau.org;

import me.fhnau.org.funcs.ReplaceDivWithMult;
import me.fhnau.org.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import static me.fhnau.org.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ReplaceDivWithMultTest {

    @Test
    public void test_simple_division() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/s");
        UCUMExpression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1");
    }

    @Test
    public void test_nothing_changed_if_no_division_present() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m.s.g");
        UCUMExpression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s1.g1");
    }

    @Test
    public void test_binary_mul_term_in_division() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s.g)");
        UCUMExpression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1.g-1");
    }

    @Test
    public void test_binary_div_term_in_division() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s/g)");
        UCUMExpression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1.g1");
    }

    @Test
    public void test_deep_binary_div_term_in_division() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s/(g/C))");
        UCUMExpression.Term replacedDivs = ReplaceDivWithMult.replaceDivWithMult(term);
        assertThat(print(replacedDivs))
                .isEqualTo("m1.s-1.g1.C-1");
    }
}
