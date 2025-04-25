package org.example.builders;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.groups.Tuple;
import org.example.UCUMDefinition;
import org.example.UCUMRegistry;
import org.example.model.Expression;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.example.TestUtil.*;

public class BuilderUtil {

    static final List<UCUMDefinition.UCUMPrefix> PREFIXES = List.of(giga, dezi);
    static final List<UCUMDefinition.UCUMUnit> UNITS = List.of(meter, gram, newton, feet, celsius, hp_c);
    static final List<Integer> EXPONENTS = List.of(-1, 0, 1, 2);
    static final List<String> ANNOTATIONS = List.of("aspirin", "oral");




    public static AbstractObjectAssert<?, Expression.Component> assert_component_term_of(Expression.Term term) {
        return assertThat(term)
                .isInstanceOf(Expression.ComponentTerm.class)
                .extracting(t -> ((Expression.ComponentTerm) t).component());
    }

    public static AbstractObjectAssert<?, Expression.Unit> assert_component_term_component_no_exponent_of(Expression.Term term) {
        return assert_component_term_of(term)
                .isInstanceOf(Expression.ComponentNoExponent.class)
                .extracting(Expression.Component::unit);
    }

    public static AbstractObjectAssert<?, Tuple> assert_component_term_component_no_exponent_prefix_simple_unit_of(Expression.Term term) {
        return assert_component_term_component_no_exponent_of(term)
                .isInstanceOf(Expression.PrefixSimpleUnit.class)
                .extracting(u -> ((Expression.PrefixSimpleUnit) u))
                .extracting(u -> tuple(u.prefix(), u.ucumUnit()));
    }

    public static AbstractObjectAssert<?, Expression.NoPrefixSimpleUnit> assert_component_term_component_no_exponent_no_prefix_simple_unit_of(Expression.Term term) {
        return assert_component_term_component_no_exponent_of(term)
                .isInstanceOf(Expression.NoPrefixSimpleUnit.class)
                .extracting(u -> ((Expression.NoPrefixSimpleUnit) u));
    }

    public static AbstractObjectAssert<?, String> assert_annot_only_term_of(Expression.Term term) {
        return assertThat(term)
                .isNotNull()
                .isInstanceOf(Expression.AnnotOnlyTerm.class)
                .extracting(Expression.AnnotOnlyTerm.class::cast)
                .extracting(Expression.AnnotOnlyTerm::annotation)
                .extracting(Expression.Annotation::annotation);
    }

    public static AbstractObjectAssert<?, Expression.ComponentExponent> assert_component_term_component_exponent(Expression.Term term) {
        return assert_component_term_of(term)
                .isInstanceOf(Expression.ComponentExponent.class)
                .extracting(Expression.ComponentExponent.class::cast);
    }

    public static AbstractObjectAssert<?, Tuple> assert_component_term_component_exponent_no_prefix_simple_unit_of(Expression.Term term) {
        return assert_component_term_component_exponent(term)
                .satisfies(componentExponent -> assert_unit_no_prefix_simple_unit_of(componentExponent.unit()))
                .extracting(c -> tuple(((Expression.NoPrefixSimpleUnit) c.unit()).ucumUnit(), c.exponent().exponent()));
    }

    public static AbstractObjectAssert<?, Tuple> assert_component_term_component_exponent_prefix_simple_unit_of(Expression.Term term) {
        return assert_component_term_component_exponent(term)
                .satisfies(componentExponent -> assert_unit_prefix_simple_unit_of(componentExponent.unit()))
                .extracting(c -> {
                    Expression.PrefixSimpleUnit prefixSimpleUnit = (Expression.PrefixSimpleUnit) c.unit();
                    return tuple(prefixSimpleUnit.prefix(), prefixSimpleUnit.ucumUnit(), c.exponent().exponent());
                });
    }


    public static AbstractObjectAssert<?, Expression.NoPrefixSimpleUnit> assert_unit_no_prefix_simple_unit_of(Expression.Unit unit) {
        return assertThat(unit)
                .isNotNull()
                .isInstanceOf(Expression.NoPrefixSimpleUnit.class)
                .extracting(Expression.NoPrefixSimpleUnit.class::cast);
    }

    public static AbstractObjectAssert<?, Expression.PrefixSimpleUnit> assert_unit_prefix_simple_unit_of(Expression.Unit unit) {
        return assertThat(unit)
                .isNotNull()
                .isInstanceOf(Expression.PrefixSimpleUnit.class)
                .extracting(Expression.PrefixSimpleUnit.class::cast);
    }

    public static AbstractObjectAssert<?, Expression.IntegerUnit> assert_unit_is_integer_unit_of(Expression.Unit unit) {
        return assertThat(unit)
                .isNotNull()
                .isInstanceOf(Expression.IntegerUnit.class)
                .extracting(Expression.IntegerUnit.class::cast);
    }

    public static void assert_term_is_no_prefix_simple_unit_with_exponent_with_annotation(Expression.Term term, UCUMDefinition.UCUMUnit expectedUnit, int expectedExponent, String expectedAnnotation) {
        assertThatCode(() -> {
            Expression.AnnotTerm annotTerm = (Expression.AnnotTerm) term;
            Expression.ComponentExponent exponent = (Expression.ComponentExponent)
                    ((Expression.ComponentTerm) annotTerm.term()).component();
            Expression.NoPrefixSimpleUnit unit = (Expression.NoPrefixSimpleUnit) exponent.unit();

            assertThat(annotTerm.annotation().annotation()).isEqualTo(expectedAnnotation);
            assertThat(exponent.exponent().exponent()).isEqualTo(expectedExponent);
            assertThat(unit.ucumUnit()).isEqualTo(expectedUnit);
        }).doesNotThrowAnyException();
    }

    public static void assert_term_is_prefix_simple_unit_with_exponent_with_annotation(Expression.Term term, UCUMDefinition.UCUMPrefix expectedPrefix, UCUMDefinition.UCUMUnit expectedUnit, int expectedExponent, String expectedAnnotation) {
        assertThatCode(() -> {
            Expression.AnnotTerm annotTerm = (Expression.AnnotTerm) term;
            Expression.ComponentExponent exponent = (Expression.ComponentExponent)
                    ((Expression.ComponentTerm) annotTerm.term()).component();
            Expression.PrefixSimpleUnit unit = (Expression.PrefixSimpleUnit) exponent.unit();

            assertThat(annotTerm.annotation().annotation()).isEqualTo(expectedAnnotation);
            assertThat(exponent.exponent().exponent()).isEqualTo(expectedExponent);
            assertThat(unit.prefix()).isEqualTo(expectedPrefix);
            assertThat(unit.ucumUnit()).isEqualTo(expectedUnit);
        }).doesNotThrowAnyException();
    }

    public static void assert_term_is_prefix_simple_unit_with_annotation(Expression.Term term, UCUMDefinition.UCUMPrefix expectedPrefix, UCUMDefinition.UCUMUnit expectedUnit, String expectedAnnotation) {
        assertThatCode(() -> {
            Expression.AnnotTerm annotTerm = (Expression.AnnotTerm) term;
            Expression.ComponentNoExponent exponent = (Expression.ComponentNoExponent)
                    ((Expression.ComponentTerm) annotTerm.term()).component();
            Expression.PrefixSimpleUnit unit = (Expression.PrefixSimpleUnit) exponent.unit();

            assertThat(annotTerm.annotation().annotation()).isEqualTo(expectedAnnotation);
            assertThat(unit.prefix()).isEqualTo(expectedPrefix);
            assertThat(unit.ucumUnit()).isEqualTo(expectedUnit);
        }).doesNotThrowAnyException();
    }

    public static Expression.Term giga_newton_exp2_annot_term() {
        return SoloTermBuilder.builder()
                .withPrefix(giga, newton)
                .asComponent()
                .withExponent(2)
                .withAnnotation("annot")
                .asTerm().build();
    }
}
