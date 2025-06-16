package io.github.fhnaumann.builders;

import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.github.fhnaumann.TestUtil.meter_term;
import static io.github.fhnaumann.builders.BuilderUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CombineTermBuilderCanonicalContractTest {

    private CombineTermBuilder.LeftStep builder;

    @BeforeEach
    public void setUp() {
        builder = CombineTermBuilder.builder();
    }

    @Test
    public void canonical_unary_div_term_from_canonical_component() {
        UCUMExpression.Term term = builder
                .unaryDiv()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.CanonicalTerm.class);
    }

    @Test
    public void canonical_binary_mul_term_from_canonical_components() {
        UCUMExpression.Term term = builder
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.CanonicalTerm.class);
    }

    @Test
    public void canonical_binary_div_term_from_canonical_components() {
        UCUMExpression.Term term = builder
                .left(meter_term())
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.CanonicalTerm.class);
    }

    @Test
    public void mixed_unary_div_term_from_mixed_component() {
        UCUMExpression.Term term = builder
                .unaryDiv()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_mul_term_from_canonical_and_mixed_components() {
        UCUMExpression.Term term = builder
                .left(meter_term())
                .multiplyWith()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_div_term_from_canonical_and_mixed_components() {
        UCUMExpression.Term term = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_mul_term_from_mixed_components() {
        UCUMExpression.Term term = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_div_term_from_mixed_components() {
        UCUMExpression.Term term = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.MixedTerm.class);
    }

    @Test
    public void nested_right_term_is_canonical_if_from_canonical_components() {
        UCUMExpression.Term inner = builder
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        UCUMExpression.Term outer = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(inner)
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((UCUMExpression.BinaryTerm) term).right())
                .isInstanceOf(UCUMExpression.CanonicalParenTerm.class);
    }

    @Test
    public void nested_right_term_is_mixed_if_from_mixed_components() {
        UCUMExpression.Term inner = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        UCUMExpression.Term outer = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(inner)
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((UCUMExpression.BinaryTerm) term).right())
                .isInstanceOf(UCUMExpression.MixedParenTerm.class);
    }
}
