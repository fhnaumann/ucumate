package me.fhnau.org.builders;

import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.groups.Tuple;

import java.util.List;

import static me.fhnau.org.TestUtil.*;
import static org.assertj.core.api.Assertions.*;

public class BuilderUtil {

    static final List<UCUMDefinition.UCUMPrefix> PREFIXES = List.of(giga, dezi);
    static final List<UCUMDefinition.UCUMUnit> UNITS = List.of(meter, gram, newton, feet, celsius, hp_c);
    static final List<Integer> EXPONENTS = List.of(-1, 0, 1, 2);
    static final List<String> ANNOTATIONS = List.of("aspirin", "oral");




    public static AbstractObjectAssert<?, UCUMExpression.Component> assert_component_term_of(UCUMExpression.Term term) {
        return assertThat(term)
                .isInstanceOf(UCUMExpression.ComponentTerm.class)
                .extracting(t -> ((UCUMExpression.ComponentTerm) t).component());
    }

    public static AbstractObjectAssert<?, UCUMExpression.Unit> assert_component_term_component_no_exponent_of(UCUMExpression.Term term) {
        return assert_component_term_of(term)
                .isInstanceOf(UCUMExpression.ComponentNoExponent.class)
                .extracting(UCUMExpression.Component::unit);
    }

    public static AbstractObjectAssert<?, Tuple> assert_component_term_component_no_exponent_prefix_simple_unit_of(UCUMExpression.Term term) {
        return assert_component_term_component_no_exponent_of(term)
                .isInstanceOf(UCUMExpression.PrefixSimpleUnit.class)
                .extracting(u -> ((UCUMExpression.PrefixSimpleUnit) u))
                .extracting(u -> tuple(u.prefix(), u.ucumUnit()));
    }

    public static AbstractObjectAssert<?, UCUMExpression.NoPrefixSimpleUnit> assert_component_term_component_no_exponent_no_prefix_simple_unit_of(UCUMExpression.Term term) {
        return assert_component_term_component_no_exponent_of(term)
                .isInstanceOf(UCUMExpression.NoPrefixSimpleUnit.class)
                .extracting(u -> ((UCUMExpression.NoPrefixSimpleUnit) u));
    }

    public static AbstractObjectAssert<?, String> assert_annot_only_term_of(UCUMExpression.Term term) {
        return assertThat(term)
                .isNotNull()
                .isInstanceOf(UCUMExpression.AnnotOnlyTerm.class)
                .extracting(UCUMExpression.AnnotOnlyTerm.class::cast)
                .extracting(UCUMExpression.AnnotOnlyTerm::annotation)
                .extracting(UCUMExpression.Annotation::annotation);
    }

    public static AbstractObjectAssert<?, UCUMExpression.ComponentExponent> assert_component_term_component_exponent(UCUMExpression.Term term) {
        return assert_component_term_of(term)
                .isInstanceOf(UCUMExpression.ComponentExponent.class)
                .extracting(UCUMExpression.ComponentExponent.class::cast);
    }

    public static AbstractObjectAssert<?, Tuple> assert_component_term_component_exponent_no_prefix_simple_unit_of(UCUMExpression.Term term) {
        return assert_component_term_component_exponent(term)
                .satisfies(componentExponent -> assert_unit_no_prefix_simple_unit_of(componentExponent.unit()))
                .extracting(c -> tuple(((UCUMExpression.NoPrefixSimpleUnit) c.unit()).ucumUnit(), c.exponent().exponent()));
    }

    public static AbstractObjectAssert<?, Tuple> assert_component_term_component_exponent_prefix_simple_unit_of(UCUMExpression.Term term) {
        return assert_component_term_component_exponent(term)
                .satisfies(componentExponent -> assert_unit_prefix_simple_unit_of(componentExponent.unit()))
                .extracting(c -> {
                    UCUMExpression.PrefixSimpleUnit prefixSimpleUnit = (UCUMExpression.PrefixSimpleUnit) c.unit();
                    return tuple(prefixSimpleUnit.prefix(), prefixSimpleUnit.ucumUnit(), c.exponent().exponent());
                });
    }


    public static AbstractObjectAssert<?, UCUMExpression.NoPrefixSimpleUnit> assert_unit_no_prefix_simple_unit_of(UCUMExpression.Unit unit) {
        return assertThat(unit)
                .isNotNull()
                .isInstanceOf(UCUMExpression.NoPrefixSimpleUnit.class)
                .extracting(UCUMExpression.NoPrefixSimpleUnit.class::cast);
    }

    public static AbstractObjectAssert<?, UCUMExpression.PrefixSimpleUnit> assert_unit_prefix_simple_unit_of(UCUMExpression.Unit unit) {
        return assertThat(unit)
                .isNotNull()
                .isInstanceOf(UCUMExpression.PrefixSimpleUnit.class)
                .extracting(UCUMExpression.PrefixSimpleUnit.class::cast);
    }

    public static AbstractObjectAssert<?, UCUMExpression.IntegerUnit> assert_unit_is_integer_unit_of(UCUMExpression.Unit unit) {
        return assertThat(unit)
                .isNotNull()
                .isInstanceOf(UCUMExpression.IntegerUnit.class)
                .extracting(UCUMExpression.IntegerUnit.class::cast);
    }

    public static void assert_term_is_no_prefix_simple_unit_with_exponent_with_annotation(UCUMExpression.Term term, UCUMDefinition.UCUMUnit expectedUnit, int expectedExponent, String expectedAnnotation) {
        assertThatCode(() -> {
            UCUMExpression.AnnotTerm annotTerm = (UCUMExpression.AnnotTerm) term;
            UCUMExpression.ComponentExponent exponent = (UCUMExpression.ComponentExponent)
                    ((UCUMExpression.ComponentTerm) annotTerm.term()).component();
            UCUMExpression.NoPrefixSimpleUnit unit = (UCUMExpression.NoPrefixSimpleUnit) exponent.unit();

            assertThat(annotTerm.annotation().annotation()).isEqualTo(expectedAnnotation);
            assertThat(exponent.exponent().exponent()).isEqualTo(expectedExponent);
            assertThat(unit.ucumUnit()).isEqualTo(expectedUnit);
        }).doesNotThrowAnyException();
    }

    public static void assert_term_is_prefix_simple_unit_with_exponent_with_annotation(UCUMExpression.Term term, UCUMDefinition.UCUMPrefix expectedPrefix, UCUMDefinition.UCUMUnit expectedUnit, int expectedExponent, String expectedAnnotation) {
        assertThatCode(() -> {
            UCUMExpression.AnnotTerm annotTerm = (UCUMExpression.AnnotTerm) term;
            UCUMExpression.ComponentExponent exponent = (UCUMExpression.ComponentExponent)
                    ((UCUMExpression.ComponentTerm) annotTerm.term()).component();
            UCUMExpression.PrefixSimpleUnit unit = (UCUMExpression.PrefixSimpleUnit) exponent.unit();

            assertThat(annotTerm.annotation().annotation()).isEqualTo(expectedAnnotation);
            assertThat(exponent.exponent().exponent()).isEqualTo(expectedExponent);
            assertThat(unit.prefix()).isEqualTo(expectedPrefix);
            assertThat(unit.ucumUnit()).isEqualTo(expectedUnit);
        }).doesNotThrowAnyException();
    }

    public static void assert_term_is_prefix_simple_unit_with_annotation(UCUMExpression.Term term, UCUMDefinition.UCUMPrefix expectedPrefix, UCUMDefinition.UCUMUnit expectedUnit, String expectedAnnotation) {
        assertThatCode(() -> {
            UCUMExpression.AnnotTerm annotTerm = (UCUMExpression.AnnotTerm) term;
            UCUMExpression.ComponentNoExponent exponent = (UCUMExpression.ComponentNoExponent)
                    ((UCUMExpression.ComponentTerm) annotTerm.term()).component();
            UCUMExpression.PrefixSimpleUnit unit = (UCUMExpression.PrefixSimpleUnit) exponent.unit();

            assertThat(annotTerm.annotation().annotation()).isEqualTo(expectedAnnotation);
            assertThat(unit.prefix()).isEqualTo(expectedPrefix);
            assertThat(unit.ucumUnit()).isEqualTo(expectedUnit);
        }).doesNotThrowAnyException();
    }

    public static UCUMExpression.Term giga_newton_exp2_annot_term() {
        return SoloTermBuilder.builder()
                .withPrefix(giga, newton)
                .asComponent()
                .withExponent(2)
                .withAnnotation("annot")
                .asTerm().build();
    }
}
