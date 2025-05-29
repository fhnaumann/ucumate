package me.fhnau.org.builders;

import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static me.fhnau.org.builders.BuilderUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SoloTermBuilderTest {

    private SoloTermBuilder.UnitStep builder;

    @BeforeEach
    public void setUp() {
        builder = SoloTermBuilder.builder();
    }

    @Test
    public void unity_is_number_1_term() {
        assertEquals(SoloTermBuilder.UNITY, builder.withIntegerUnit(1).noExpNoAnnot().asTerm().build());
    }

    @Test
    public void term_is_integer_unit() {
        UCUMExpression.Term term = builder.withIntegerUnit(5).noExpNoAnnot().asTerm().build();
        //assertThat(term, isIntegerUnitWithValue(5));
        assert_component_term_component_no_exponent_of(term)
                .satisfies(unit -> assert_unit_is_integer_unit_of(unit).satisfies(integerUnit -> assertThat(integerUnit.value()).isEqualTo(
                        5)));
    }

    @ParameterizedTest
    @MethodSource("provideUnit")
    public void term_is_no_prefix_simple_unit(UCUMDefinition.UCUMUnit unit) {
        UCUMExpression.Term term = builder
                .withoutPrefix(unit)
                .noExpNoAnnot()
                .asTerm()
                .build();
        assert_component_term_component_no_exponent_no_prefix_simple_unit_of(term)
                .extracting(UCUMExpression.SimpleUnit::ucumUnit)
                .isEqualTo(unit);
    }

    @ParameterizedTest
    @MethodSource("providePrefixAndUnit")
    public void term_is_prefix_simple_unit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) {
        UCUMExpression.Term term = builder
                .withPrefix(prefix, unit)
                .noExpNoAnnot()
                .asTerm()
                .build();
        assert_component_term_component_no_exponent_prefix_simple_unit_of(term)
                .isEqualTo(tuple(prefix, unit));
    }

    @Test
    public void term_is_only_annotation() {
        UCUMExpression.Term term = builder
                .onlyAnnotation("abc")
                .asTerm()
                .build();
        assert_annot_only_term_of(term)
                .isEqualTo("abc");
    }

    @Test
    public void term_is_only_annotation_with_parens() {
        UCUMExpression.Term term = builder
                .onlyAnnotation("abc")
                .asTermWithParens()
                .asTerm()
                .build();
        assertThat(term).isInstanceOf(UCUMExpression.ParenTerm.class);
        assert_annot_only_term_of(((UCUMExpression.ParenTerm) term).term())
                .isEqualTo("abc");

    }

    @ParameterizedTest
    @MethodSource("provideUnitAndExponent")
    public void term_is_no_prefix_simple_unit_with_exponent(UCUMDefinition.UCUMUnit unit, int exponent) {
        UCUMExpression.Term term = builder
                .withoutPrefix(unit)
                .asComponent()
                .withExponent(exponent)
                .withoutAnnotation()
                .asTerm().build();
        assert_component_term_component_exponent_no_prefix_simple_unit_of(term).isEqualTo(tuple(unit, exponent));
    }

    @ParameterizedTest
    @MethodSource("providePrefixAndUnitAndExponent")
    public void term_is_prefix_simple_unit_with_exponent(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit, int exponent) {
        UCUMExpression.Term term = builder
                .withPrefix(prefix, unit)
                .asComponent()
                .withExponent(exponent)
                .withoutAnnotation()
                .asTerm().build();
        assert_component_term_component_exponent_prefix_simple_unit_of(term).isEqualTo(tuple(prefix, unit, exponent));
    }

    @ParameterizedTest
    @MethodSource("providePrefixAndUnitAndExponentAndAnnotation")
    public void term_is_prefix_simple_unit_with_exponent_with_annotation(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit, int exponent, String annotation) {
        UCUMExpression.Term term = builder
                .withPrefix(prefix, unit)
                .asComponent()
                .withExponent(exponent)
                .withAnnotation(annotation)
                .asTerm().build();
        assert_term_is_prefix_simple_unit_with_exponent_with_annotation(term, prefix, unit, exponent, annotation);

        /*
        assertThat(term)
                .isInstanceOf(Expression.AnnotTerm.class)
                .extracting(Expression.AnnotTerm.class::cast)
                .satisfies(annotTerm -> assertThat(annotTerm.annotation().annotation()).isEqualTo("abc"))
                .extracting(Expression.AnnotTerm::term)
                .isInstanceOf(Expression.ComponentTerm.class)
                .extracting(Expression.ComponentTerm.class::cast)
                .extracting(Expression.ComponentTerm::component)
                .isInstanceOf(Expression.ComponentExponent.class)
                .extracting(Expression.ComponentExponent.class::cast)
                .satisfies(componentExponent -> assertThat(componentExponent.exponent().exponent()).isEqualTo(2))
                .extracting(Expression.Component::unit)
                .isInstanceOf(Expression.PrefixSimpleUnit.class)
                .extracting(Expression.PrefixSimpleUnit.class::cast)
                .extracting(prefixSimpleUnit -> tuple(prefixSimpleUnit.prefix(), prefixSimpleUnit.ucumUnit()))
                .isEqualTo(tuple(dezi, meter));*/
    }

    @ParameterizedTest
    @MethodSource("provideUnitAndExponentAndAnnotation")
    public void term_is_no_prefix_simple_unit_with_exponent_with_annotation(UCUMDefinition.UCUMUnit unit, int exponent, String annotation) {
        UCUMExpression.Term term = builder
                .withoutPrefix(unit)
                .asComponent()
                .withExponent(exponent)
                .withAnnotation(annotation)
                .asTerm().build();
        assert_term_is_no_prefix_simple_unit_with_exponent_with_annotation(term, unit, exponent, annotation);
    }

    @ParameterizedTest
    @MethodSource("providePrefixAndUnitAndExponentAndAnnotation")
    public void term_is_prefix_simple_unit_with_exponent_with_annotation_with_parens(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit, int exponent, String annotation) {
        UCUMExpression.Term term = builder
                .withPrefix(prefix, unit)
                .asComponent()
                .withExponent(exponent)
                .withAnnotation(annotation)
                .asTermWithParens()
                .asTerm().build();
        assertThat(term).isInstanceOf(UCUMExpression.ParenTerm.class);
        assert_term_is_prefix_simple_unit_with_exponent_with_annotation(((UCUMExpression.ParenTerm) term).term(),
                                                                        prefix,
                                                                        unit,
                                                                        exponent,
                                                                        annotation
        );
    }


    @ParameterizedTest
    @MethodSource("provideUnitAndExponentAndAnnotation")
    public void term_is_no_prefix_simple_unit_with_exponent_with_annotation_with_parens(UCUMDefinition.UCUMUnit unit, int exponent, String annotation) {
        UCUMExpression.Term term = builder
                .withoutPrefix(unit)
                .asComponent()
                .withExponent(exponent)
                .withAnnotation(annotation)
                .asTermWithParens()
                .asTerm().build();
        assertThat(term).isInstanceOf(UCUMExpression.ParenTerm.class);
        assert_term_is_no_prefix_simple_unit_with_exponent_with_annotation(((UCUMExpression.ParenTerm) term).term(),
                                                                           unit,
                                                                           exponent,
                                                                           annotation
        );
    }

    @ParameterizedTest
    @MethodSource("providePrefixAndUnitAndAnnotation")
    public void term_is_prefix_simple_unit_with_annotation(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit, String annotation) {
        UCUMExpression.Term term = builder
                .withPrefix(prefix, unit)
                .asComponent()
                .withoutExponent()
                .withAnnotation(annotation)
                .asTerm().build();
        assert_term_is_prefix_simple_unit_with_annotation(term, prefix, unit, annotation);
    }

    @Test
    public void no_exp_no_annot_short_circuit_is_same_as_long_variant() {
        UCUMExpression.Term termUsingShortCircuit = builder
                .withIntegerUnit(5)
                .noExpNoAnnot()
                .asTerm().build();
        UCUMExpression.Term termUsingLongVariant = builder
                .withIntegerUnit(5)
                .asComponent()
                .withoutExponent()
                .withoutAnnotation()
                .asTerm().build();
        assertEquals(termUsingShortCircuit, termUsingLongVariant);
    }

    static Stream<Arguments> provideUnit() {
        return UNITS.stream().map(Arguments::of);

    }

    static Stream<Arguments> providePrefixAndUnit() {
        return PREFIXES.stream().flatMap(
                prefix -> UNITS.stream().map(
                        unit -> Arguments.of(prefix, unit)
                )
        );
    }

    static Stream<Arguments> provideUnitAndExponent() {
        return UNITS.stream().flatMap(
                unit -> EXPONENTS.stream().map(
                        exponent -> Arguments.of(unit, exponent)
                )
        );
    }

    static Stream<Arguments> providePrefixAndUnitAndExponent() {
        return PREFIXES.stream().flatMap(
                prefix -> UNITS.stream().flatMap(
                        unit -> EXPONENTS.stream().map(
                                exponent -> Arguments.of(prefix, unit, exponent)
                        )
                )
        );
    }

    static Stream<Arguments> providePrefixAndUnitAndAnnotation() {
        return PREFIXES.stream().flatMap(
                prefix -> UNITS.stream().flatMap(
                        unit -> ANNOTATIONS.stream().map(
                                annotation -> Arguments.of(prefix, unit, annotation)
                        )
                )
        );
    }

    static Stream<Arguments> provideUnitAndExponentAndAnnotation() {
        return UNITS.stream().flatMap(
                unit -> EXPONENTS.stream().flatMap(
                        exp -> ANNOTATIONS.stream().map(
                                annot -> Arguments.of(unit, exp, annot)
                        )
                )
        );
    }

    static Stream<Arguments> providePrefixAndUnitAndExponentAndAnnotation() {
        return PREFIXES.stream().flatMap(
                prefix -> UNITS.stream().flatMap(
                        unit -> EXPONENTS.stream().flatMap(
                                exp -> ANNOTATIONS.stream().map(
                                        annot -> Arguments.of(prefix, unit, exp, annot)
                                )
                        )
                )
        );

    }

}
