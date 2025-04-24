package org.example.builders;

import org.example.model.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.example.TestUtil.meter_term;
import static org.junit.jupiter.api.Assertions.*;
import static org.example.builders.BuilderUtil.*;

public class CombineTermBuilderCanonicalContractTest {

    private CombineTermBuilder.LeftStep builder;

    @BeforeEach
    public void setUp() {
        builder = CombineTermBuilder.builder();
    }

    @Test
    public void canonical_unary_div_term_from_canonical_component() {
        Expression.Term term = builder
                .unaryDiv()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void canonical_binary_mul_term_from_canonical_components() {
        Expression.Term term = builder
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void canonical_binary_div_term_from_canonical_components() {
        Expression.Term term = builder
                .left(meter_term())
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void mixed_unary_div_term_from_mixed_component() {
        Expression.Term term = builder
                .unaryDiv()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_mul_term_from_canonical_and_mixed_components() {
        Expression.Term term = builder
                .left(meter_term())
                .multiplyWith()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_div_term_from_canonical_and_mixed_components() {
        Expression.Term term = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_mul_term_from_mixed_components() {
        Expression.Term term = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void mixed_binary_div_term_from_mixed_components() {
        Expression.Term term = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void nested_left_term_is_canonical_if_from_canonical_components() {
        Expression.Term inner = builder
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        Expression.Term outer = builder
                .left(inner)
                .divideBy()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((Expression.BinaryTerm) term).left())
                .isInstanceOf(Expression.CanonicalParenTerm.class);
    }

    @Test
    public void nested_left_term_is_mixed_if_from_mixed_components() {
        Expression.Term inner = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        Expression.Term outer = builder
                .left(inner)
                .divideBy()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((Expression.BinaryTerm) term).left())
                .isInstanceOf(Expression.MixedParenTerm.class);
    }

    @Test
    public void nested_right_term_is_canonical_if_from_canonical_components() {
        Expression.Term inner = builder
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        Expression.Term outer = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(inner)
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((Expression.BinaryTerm) term).right())
                .isInstanceOf(Expression.CanonicalParenTerm.class);
    }

    @Test
    public void nested_right_term_is_mixed_if_from_mixed_components() {
        Expression.Term inner = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        Expression.Term outer = builder
                .left(giga_newton_exp2_annot_term())
                .divideBy()
                .right(inner)
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((Expression.BinaryTerm) term).right())
                .isInstanceOf(Expression.MixedParenTerm.class);
    }
}
