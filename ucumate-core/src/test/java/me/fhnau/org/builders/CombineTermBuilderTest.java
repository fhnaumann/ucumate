package me.fhnau.org.builders;

import me.fhnau.org.model.UCUMExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static me.fhnau.org.TestUtil.*;
import static me.fhnau.org.builders.BuilderUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CombineTermBuilderTest {

    private CombineTermBuilder.LeftStep builder;

    @BeforeEach
    public void setUp() {
        builder = CombineTermBuilder.builder();
    }

    @Test
    public void term_is_unary_div() {
        UCUMExpression.Term term = builder.unaryDiv().right(meter_term()).build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.UnaryDivTerm.class)
                .extracting(UCUMExpression.UnaryDivTerm.class::cast)
                .extracting(UCUMExpression.UnaryDivTerm::term)
                .isEqualTo(meter_term());
    }

    @Test
    public void term_is_binary_mul() {
        UCUMExpression.Term term = builder
                .left(meter_term())
                .multiplyWith()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.BinaryTerm.class)
                .extracting(UCUMExpression.BinaryTerm.class::cast)
                .extracting(binaryTerm -> tuple(binaryTerm.left(), binaryTerm.operator(), binaryTerm.right()))
                .isEqualTo(tuple(meter_term(), UCUMExpression.Operator.MUL, giga_newton_exp2_annot_term()));
    }

    @Test
    public void term_is_binary_div() {
        UCUMExpression.Term term = builder
                .left(meter_term())
                .divideBy()
                .right(giga_newton_exp2_annot_term())
                .build();
        assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.BinaryTerm.class)
                .extracting(UCUMExpression.BinaryTerm.class::cast)
                .extracting(binaryTerm -> tuple(binaryTerm.left(), binaryTerm.operator(), binaryTerm.right()))
                .isEqualTo(tuple(meter_term(), UCUMExpression.Operator.DIV, giga_newton_exp2_annot_term()));
    }

    @Test
    @DisplayName("Test if 'GN^2{annot}/(m.m)' has the '()'")
    public void nested_mul_term_has_parens() {
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
                .satisfies(term -> assertThat(term)
                        .isNotNull()
                        .isInstanceOf(UCUMExpression.ParenTerm.class)
                        .extracting(term1 -> ((UCUMExpression.ParenTerm) term1).term())
                        .isEqualTo(inner)
                );
    }

    @Test
    @DisplayName("Test if 'GN^2{annot}.(m/m)' has the '()'")
    public void nested_div_term_has_parens() {
        UCUMExpression.Term inner = builder
                .left(meter_term())
                .divideBy()
                .right(meter_term())
                .build();
        UCUMExpression.Term outer = builder
                .left(giga_newton_exp2_annot_term())
                .multiplyWith()
                .right(inner)
                .build();
        assertThat(outer)
                .isNotNull()
                .extracting(term -> ((UCUMExpression.BinaryTerm) term).right())
                .satisfies(term -> assertThat(term)
                        .isNotNull()
                        .isInstanceOf(UCUMExpression.ParenTerm.class)
                        .extracting(term1 -> ((UCUMExpression.ParenTerm) term1).term())
                        .isEqualTo(inner)
                );
    }

    @ParameterizedTest
    @MethodSource("provide_terms_that_dont_require_parens")
    public void term_is_not_wrapped_in_parens(UCUMExpression.Term term) {
        UCUMExpression.Term outer = builder
                .left(term)
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(outer)
                .extracting(term1 -> ((UCUMExpression.BinaryTerm) term1).left())
                .isNotInstanceOf(UCUMExpression.ParenTerm.class);
    }

    @ParameterizedTest
    @MethodSource("provide_terms_that_require_parens")
    public void term_is_wrapped_in_parens(UCUMExpression.Term term) {
        UCUMExpression.Term outer = builder
                .left(term)
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(outer)
                .extracting(term1 -> ((UCUMExpression.BinaryTerm) term1).left())
                .isInstanceOf(UCUMExpression.ParenTerm.class);
    }

    @Test
    public void paren_term_is_not_wrapped_in_parens() {
        UCUMExpression.Term outer = builder
                .left(meter_parens_term())
                .divideBy()
                .right(meter_term())
                .build();
        assertThat(outer)
                .extracting(term -> ((UCUMExpression.BinaryTerm) term).left())
                .extracting(term -> ((UCUMExpression.ParenTerm) term).term())
                .isNotInstanceOf(UCUMExpression.ParenTerm.class);
    }

    static Stream<UCUMExpression.Term> provide_terms_that_dont_require_parens() {
        UCUMExpression.Term componentTerm = SoloTermBuilder.builder()
                .withoutPrefix(meter)
                .noExpNoAnnot()
                .asTerm().build();
        UCUMExpression.Term annotOnly = SoloTermBuilder.builder()
                                                   .onlyAnnotation("annot")
                                                   .asTerm().build();
        UCUMExpression.Term annotTerm = SoloTermBuilder.builder()
                .withoutPrefix(meter)
                .asComponent()
                .withoutExponent()
                .withAnnotation("annot")
                .asTerm().build();
        return Stream.of(componentTerm, annotOnly, annotTerm);
    }

    static Stream<UCUMExpression.Term> provide_terms_that_require_parens() {
        UCUMExpression.Term binaryTerm = CombineTermBuilder.builder()
                .left(meter_term())
                .multiplyWith()
                .right(meter_term())
                .build();
        UCUMExpression.Term unaryDivTerm = CombineTermBuilder.builder()
                .unaryDiv()
                .right(meter_term())
                .build();
        return Stream.of(binaryTerm, unaryDivTerm);
    }
}
