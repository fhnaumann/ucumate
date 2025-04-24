package org.example.builders;


import org.example.funcs.PrettyPrinter;
import org.example.model.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.example.builders.BuilderUtil.*;
import static org.example.TestUtil.*;

public class CombineTermBuilderTest {

    private CombineTermBuilder.LeftStep builder;

    @BeforeEach
    public void setUp() {
        builder = CombineTermBuilder.builder();
    }

    @Test
    public void term_is_unary_div() {
        Expression.Term term = builder.unaryDiv().right(meter_term()).build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.UnaryDivTerm.class)
                .extracting(Expression.UnaryDivTerm.class::cast)
                .extracting(Expression.UnaryDivTerm::term)
                .isEqualTo(meter_term());
    }

    @Test
    public void term_is_binary_mul() {
        Expression.Term term = builder
                .left(meter_term())
                .multiplyWith()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.BinaryTerm.class)
                .extracting(Expression.BinaryTerm.class::cast)
                .extracting(binaryTerm -> tuple(binaryTerm.left(), binaryTerm.operator(), binaryTerm.right()))
                .isEqualTo(tuple(meter_term(), Expression.Operator.MUL, giga_newton_exp2_annot_term()));
    }

    @Test
    public void term_is_binary_div() {
        Expression.Term term = builder
                .left(meter_term())
                .divideBy()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.BinaryTerm.class)
                .extracting(Expression.BinaryTerm.class::cast)
                .extracting(binaryTerm -> tuple(binaryTerm.left(), binaryTerm.operator(), binaryTerm.right()))
                .isEqualTo(tuple(meter_term(), Expression.Operator.DIV, giga_newton_exp2_annot_term()));
    }

    @Test
    @DisplayName("Test if 'GN^2{annot}/(m.m)' has the '()'")
    public void nested_mul_term_has_parens() {
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
                .satisfies(term -> assertThat(term)
                        .isNotNull()
                        .isInstanceOf(Expression.ParenTerm.class)
                        .extracting(term1 -> ((Expression.ParenTerm) term1).term())
                        .isEqualTo(inner)
                );
    }

    @Test
    @DisplayName("Test if 'GN^2{annot}.(m/m)' has the '()'")
    public void nested_div_term_has_parens() {
        Expression.Term inner = builder
                .left(meter_term())
                .divideBy()
                .right(meter_term())
                .build();
        Expression.Term outer = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(inner)
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((Expression.BinaryTerm) term).right())
                .satisfies(term -> assertThat(term)
                        .isNotNull()
                        .isInstanceOf(Expression.ParenTerm.class)
                        .extracting(term1 -> ((Expression.ParenTerm) term1).term())
                        .isEqualTo(inner)
                );
    }

    @ParameterizedTest
    @MethodSource("provide_terms_that_dont_require_parens")
    public void term_is_not_wrapped_in_parens(Expression.Term term) {
        Expression.Term outer = builder
                .left(term)
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(outer)
                .extracting(term1 -> ((Expression.BinaryTerm) term1).left())
                .isNotInstanceOf(Expression.ParenTerm.class);
    }

    @ParameterizedTest
    @MethodSource("provide_terms_that_require_parens")
    public void term_is_wrapped_in_parens(Expression.Term term) {
        Expression.Term outer = builder
                .left(term)
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(outer)
                .extracting(term1 -> ((Expression.BinaryTerm) term1).left())
                .isInstanceOf(Expression.ParenTerm.class);
    }

    @Test
    public void paren_term_is_not_wrapped_in_parens() {
        Expression.Term outer = builder
                .left(meter_parens_term())
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(outer)
                .extracting(term -> ((Expression.BinaryTerm) term).left())
                .extracting(term -> ((Expression.ParenTerm) term).term())
                .isNotInstanceOf(Expression.ParenTerm.class);
    }

    static Stream<Expression.Term> provide_terms_that_dont_require_parens() {
        Expression.Term componentTerm = SoloTermBuilder.builder()
                .withoutPrefix(meter)
                .noExpNoAnnot()
                .asTerm().build();
        Expression.Term annotOnly = SoloTermBuilder.builder()
                                                   .onlyAnnotation("annot")
                                                   .asTerm().build();
        Expression.Term annotTerm = SoloTermBuilder.builder()
                .withoutPrefix(meter)
                .asComponent()
                .withoutExponent()
                .withAnnotation("annot")
                .asTerm().build();
        return Stream.of(componentTerm, annotOnly, annotTerm);
    }

    static Stream<Expression.Term> provide_terms_that_require_parens() {
        Expression.Term binaryTerm = CombineTermBuilder.builder()
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        Expression.Term unaryDivTerm = CombineTermBuilder.builder()
                .unaryDiv()
                .right(meter_term())
                .build();
        return Stream.of(binaryTerm, unaryDivTerm);
    }
}
