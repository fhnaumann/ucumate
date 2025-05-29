package me.fhnau.org.model;


import me.fhnau.org.util.PreciseDecimal;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
)
public sealed interface UCUMExpression permits UCUMExpression.Operator, UCUMExpression.Annotation, UCUMExpression.CanoncialUCUMExpression, UCUMExpression.Component, UCUMExpression.Exponent, UCUMExpression.MixedUCUMExpression, UCUMExpression.Term, UCUMExpression.Unit {

    sealed interface CanoncialUCUMExpression extends UCUMExpression {}
    sealed interface MixedUCUMExpression extends UCUMExpression {}

    /*
    Unit Definitions
     */
    sealed interface Unit extends UCUMExpression {}

    sealed interface CanonicalUnit extends CanoncialUCUMExpression, Unit {}
    sealed interface MixedUnit extends MixedUCUMExpression, Unit {}

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
        public PreciseDecimal asPreciseDecimal() {
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
    sealed interface Component extends UCUMExpression {
        Unit unit();
    }

    sealed interface CanonicalComponent extends CanoncialUCUMExpression, Component {
        @Override
        CanonicalUnit unit();
    }
    sealed interface MixedComponent extends MixedUCUMExpression, Component {}

    sealed interface ComponentExponent extends Component {
        Exponent exponent();
    }
    sealed interface ComponentNoExponent extends Component {}

    record Exponent(int exponent) implements UCUMExpression {}

    record CanonicalComponentExponent(@NotNull CanonicalUnit unit, @NotNull Exponent exponent) implements CanonicalComponent, ComponentExponent {}
    record CanonicalComponentNoExponent(@NotNull CanonicalUnit unit) implements CanonicalComponent, ComponentNoExponent {}
    record MixedComponentExponent(@NotNull Unit unit, @NotNull Exponent exponent) implements MixedComponent, ComponentExponent {}
    record MixedComponentNoExponent(@NotNull Unit unit) implements MixedComponent, ComponentNoExponent {}

    /*
    Term Definitions
     */
    sealed interface Term extends UCUMExpression {}

    sealed interface CanonicalTerm extends CanoncialUCUMExpression, Term {}
    sealed interface MixedTerm extends MixedUCUMExpression, Term {}

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

    record Annotation(@NotNull String annotation) implements UCUMExpression {}

    record AnnotOnlyTerm(@NotNull Annotation annotation) implements CanonicalTerm, MixedTerm {}

    record CanonicalComponentTerm(@NotNull CanonicalComponent component) implements CanonicalTerm, ComponentTerm {}
    record CanonicalAnnotTerm(@NotNull CanonicalTerm term, @NotNull Annotation annotation) implements CanonicalTerm, AnnotTerm {}
    record CanonicalParenTerm(@NotNull CanonicalTerm term) implements CanonicalTerm, ParenTerm {}
    record CanonicalBinaryTerm(@NotNull CanonicalTerm left, @NotNull Operator operator,@NotNull CanonicalTerm right) implements CanonicalTerm, BinaryTerm {}
    record CanonicalUnaryDivTerm(@NotNull CanonicalTerm term) implements CanonicalTerm, UnaryDivTerm {}
    record MixedComponentTerm(@NotNull Component component) implements MixedTerm, ComponentTerm {}
    record MixedAnnotTerm(@NotNull Term term, @NotNull Annotation annotation) implements MixedTerm, AnnotTerm {}
    record MixedParenTerm(@NotNull Term term) implements MixedTerm, ParenTerm {}
    record MixedBinaryTerm(@NotNull Term left, @NotNull Operator operator, @NotNull Term right) implements MixedTerm, BinaryTerm {}
    record MixedUnaryDivTerm(@NotNull Term term) implements MixedTerm, UnaryDivTerm {}

    enum Operator implements UCUMExpression {
        MUL, DIV
    }
}
