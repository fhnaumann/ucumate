package org.example.model;


import com.google.common.base.Preconditions;
import org.example.UCUMDefinition;
import org.example.util.PreciseDecimal;
import org.jetbrains.annotations.NotNull;

public sealed interface Expression permits Expression.Operator, Expression.Annotation, Expression.CanoncialExpression, Expression.Component, Expression.Exponent, Expression.MixedExpression, Expression.Term, Expression.Unit {

    sealed interface CanoncialExpression extends Expression {}
    sealed interface MixedExpression extends Expression {}

    /*
    Unit Definitions
     */
    sealed interface Unit extends Expression {}

    sealed interface CanonicalUnit extends CanoncialExpression, Unit {}
    sealed interface MixedUnit extends MixedExpression, Unit {}

    sealed interface SimpleUnit extends Unit {
        @NotNull UCUMDefinition.UCUMUnit ucumUnit();
    }
    sealed interface CanonicalSimpleUnit extends CanonicalUnit, SimpleUnit {
        @Override
        @NotNull UCUMDefinition.BaseUnit ucumUnit();
    }
    sealed interface MixedSimpleUnit extends MixedUnit, SimpleUnit {}
    sealed interface PrefixSimpleUnit extends SimpleUnit {
        @NotNull UCUMDefinition.UCUMPrefix prefix();
    }
    sealed interface NoPrefixSimpleUnit extends SimpleUnit {}

    record IntegerUnit(int value) implements CanonicalUnit {
        PreciseDecimal asPreciseDecimal() {
            return new PreciseDecimal(Integer.toString(value));
        }
    }

    record CanonicalPrefixSimpleUnit(@NotNull UCUMDefinition.UCUMPrefix prefix, @NotNull UCUMDefinition.BaseUnit ucumUnit) implements CanonicalSimpleUnit, PrefixSimpleUnit {}
    record CanonicalNoPrefixSimpleUnit(@NotNull UCUMDefinition.BaseUnit ucumUnit) implements CanonicalSimpleUnit, NoPrefixSimpleUnit {}
    record MixedPrefixSimpleUnit(@NotNull UCUMDefinition.UCUMPrefix prefix, @NotNull UCUMDefinition.UCUMUnit ucumUnit) implements MixedSimpleUnit, PrefixSimpleUnit {}
    record MixedNoPrefixSimpleUnit(@NotNull UCUMDefinition.UCUMUnit ucumUnit) implements MixedSimpleUnit, NoPrefixSimpleUnit {}

    /*
    Component Definitions
     */
    sealed interface Component extends Expression {
        Unit unit();
    }

    sealed interface CanonicalComponent extends CanoncialExpression, Component {
        @Override
        CanonicalUnit unit();
    }
    sealed interface MixedComponent extends MixedExpression, Component {}

    sealed interface ComponentExponent extends Component {
        Exponent exponent();
    }
    sealed interface ComponentNoExponent extends Component {}

    record Exponent(int exponent) implements Expression {}

    record CanonicalComponentExponent(@NotNull CanonicalUnit unit, @NotNull Exponent exponent) implements CanonicalComponent, ComponentExponent {}
    record CanonicalComponentNoExponent(@NotNull CanonicalUnit unit) implements CanonicalComponent, ComponentNoExponent {}
    record MixedComponentExponent(@NotNull Unit unit, @NotNull Exponent exponent) implements MixedComponent, ComponentExponent {}
    record MixedComponentNoExponent(@NotNull Unit unit) implements MixedComponent, ComponentNoExponent {}

    /*
    Term Definitions
     */
    sealed interface Term extends Expression {}

    sealed interface CanonicalTerm extends CanoncialExpression, Term {}
    sealed interface MixedTerm extends MixedExpression, Term {}

    sealed interface ComponentTerm extends Term {
        @NotNull Component component();
    }
    sealed interface AnnotTerm extends Term {
        @NotNull Term term();
        @NotNull Annotation annotation();
    }
    sealed interface ParenTerm extends Term {
        @NotNull Term term();
    }
    sealed interface BinaryTerm extends Term {
        @NotNull Term left();
        @NotNull Operator operator();
        @NotNull Term right();
    }
    sealed interface UnaryDivTerm extends Term {
        @NotNull Term term();
        default Operator operator() {
            return Operator.DIV;
        }
    }

    record Annotation(@NotNull String annotation) implements Expression {}

    record AnnotOnlyTerm(@NotNull Annotation annotation) implements CanonicalTerm, MixedTerm {}

    record CanonicalComponentTerm(@NotNull CanonicalComponent component) implements CanonicalTerm, ComponentTerm {}
    record CanonicalAnnotTerm(@NotNull CanonicalComponentTerm term, @NotNull Annotation annotation) implements CanonicalTerm, AnnotTerm {}
    record CanonicalParenTerm(@NotNull CanonicalTerm term) implements CanonicalTerm, ParenTerm {}
    record CanonicalBinaryTerm(@NotNull CanonicalTerm left, @NotNull Operator operator,@NotNull CanonicalTerm right) implements CanonicalTerm, BinaryTerm {}
    record CanonicalUnaryDivTerm(@NotNull CanonicalTerm term) implements CanonicalTerm, UnaryDivTerm {}
    record MixedComponentTerm(@NotNull Component component) implements MixedTerm, ComponentTerm {}
    record MixedAnnotTerm(@NotNull Term term, @NotNull Annotation annotation) implements MixedTerm, AnnotTerm {}
    record MixedParenTerm(@NotNull Term term) implements MixedTerm, ParenTerm {}
    record MixedBinaryTerm(@NotNull Term left, @NotNull Operator operator, @NotNull Term right) implements MixedTerm, BinaryTerm {}
    record MixedUnaryDivTerm(@NotNull Term term) implements MixedTerm, UnaryDivTerm {}

    enum Operator implements Expression {
        MUL, DIV
    }
}
