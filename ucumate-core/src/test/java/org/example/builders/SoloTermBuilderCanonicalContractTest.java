package org.example.builders;

import org.example.model.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.example.builders.BuilderUtil.*;
import static org.example.TestUtil.*;

public class SoloTermBuilderCanonicalContractTest {

    private SoloTermBuilder.UnitStep builder;

    @BeforeEach
    public void setUp() {
        builder = SoloTermBuilder.builder();
    }

    @Test
    public void canonical_simple_unit_yields_canonical_term() {
        Expression.Term term = builder
                .withoutPrefix(gram)
                .noExpNoAnnot()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void mixed_simple_unit_yields_mixed_term() {
        Expression.Term term = builder
                .withoutPrefix(newton)
                .noExpNoAnnot()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void canonical_prefix_simple_unit_yields_canonical_term() {
        Expression.Term term = builder
                .withPrefix(giga, gram)
                .noExpNoAnnot()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void mixed_prefix_simple_unit_yields_mixed_term() {
        Expression.Term term = builder
                .withPrefix(giga, hp_c)
                .noExpNoAnnot()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void canonical_simple_unit_exponent_yields_canonical_term() {
        Expression.Term term = builder
                .withoutPrefix(gram)
                .asComponent()
                .withExponent(2)
                .withoutAnnotation()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void mixed_simple_unit_exponent_yields_mixed_term() {
        Expression.Term term = builder
                .withoutPrefix(feet)
                .asComponent()
                .withExponent(2)
                .withoutAnnotation()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void canonical_simple_unit_annotation_yields_canonical_term() {
        Expression.Term term = builder
                .withoutPrefix(gram)
                .asComponent()
                .withoutExponent()
                .withAnnotation("annot")
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void mixed_simple_unit_annotation_yields_mixed_term() {
        Expression.Term term = builder
                .withoutPrefix(newton)
                .asComponent()
                .withoutExponent()
                .withAnnotation("annot")
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(Expression.MixedTerm.class);
    }

    @Test
    public void canonical_only_annotation_yields_canonical_term() {
        Expression.Term term = builder.onlyAnnotation("annot").asTerm().build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void integer_simple_unit_yields_canonical_term() {
        Expression.Term term = builder.withIntegerUnit(5).noExpNoAnnot().asTerm().build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void integer_simple_unit_exponent_yields_canonical_term() {
        Expression.Term term = builder
                .withIntegerUnit(5)
                .asComponent()
                .withExponent(2)
                .withoutAnnotation()
                .asTerm().build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }

    @Test
    public void integer_simple_unit_annotation_yields_canonical_term() {
        Expression.Term term = builder
                .withIntegerUnit(5)
                .asComponent()
                .withoutExponent()
                .withAnnotation("annot")
                .asTerm().build();
        assertThat(term).isInstanceOf(Expression.CanonicalTerm.class);
    }
}
