package me.fhnau.org.model;


import me.fhnau.org.util.PreciseDecimal;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
        UCUMDefinition.UCUMUnit ucumUnit();
    }
    sealed interface CanonicalSimpleUnit extends CanonicalUnit, SimpleUnit {
        @Override
        UCUMDefinition.BaseUnit ucumUnit();
    }
    sealed interface MixedSimpleUnit extends MixedUnit, SimpleUnit {}
    sealed interface PrefixSimpleUnit extends SimpleUnit {
        UCUMDefinition.UCUMPrefix prefix();
    }
    sealed interface NoPrefixSimpleUnit extends SimpleUnit {}

    record IntegerUnit(int value) implements CanonicalUnit {
        public PreciseDecimal asPreciseDecimal() {
            return new PreciseDecimal(Integer.toString(value));
        }
    }

    record CanonicalPrefixSimpleUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.BaseUnit ucumUnit) implements CanonicalSimpleUnit, PrefixSimpleUnit {}
    record CanonicalNoPrefixSimpleUnit(UCUMDefinition.BaseUnit ucumUnit) implements CanonicalSimpleUnit, NoPrefixSimpleUnit {}
    record MixedPrefixSimpleUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit ucumUnit) implements MixedSimpleUnit, PrefixSimpleUnit {}
    record MixedNoPrefixSimpleUnit(UCUMDefinition.UCUMUnit ucumUnit) implements MixedSimpleUnit, NoPrefixSimpleUnit {}

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

    record CanonicalComponentExponent(CanonicalUnit unit, Exponent exponent) implements CanonicalComponent, ComponentExponent {}
    record CanonicalComponentNoExponent(CanonicalUnit unit) implements CanonicalComponent, ComponentNoExponent {}
    record MixedComponentExponent(Unit unit, Exponent exponent) implements MixedComponent, ComponentExponent {}
    record MixedComponentNoExponent(Unit unit) implements MixedComponent, ComponentNoExponent {}

    /*
    Term Definitions
     */
    sealed interface Term extends UCUMExpression {}

    sealed interface CanonicalTerm extends CanoncialUCUMExpression, Term {}
    sealed interface MixedTerm extends MixedUCUMExpression, Term {}

    sealed interface ComponentTerm extends Term {
        Component component();
    }
    sealed interface AnnotTerm extends Term {
        Term term();
        Annotation annotation();
    }
    sealed interface ParenTerm extends Term {
        Term term();
    }
    sealed interface BinaryTerm extends Term {
        Term left();
        Operator operator();
        Term right();
    }
    sealed interface UnaryDivTerm extends Term {
        Term term();
        default Operator operator() {
            return Operator.DIV;
        }
    }

    record Annotation(String annotation) implements UCUMExpression {}

    record AnnotOnlyTerm(Annotation annotation) implements CanonicalTerm, MixedTerm {}

    record CanonicalComponentTerm(CanonicalComponent component) implements CanonicalTerm, ComponentTerm {}
    record CanonicalAnnotTerm(CanonicalTerm term, Annotation annotation) implements CanonicalTerm, AnnotTerm {}
    record CanonicalParenTerm(CanonicalTerm term) implements CanonicalTerm, ParenTerm {}
    record CanonicalBinaryTerm(CanonicalTerm left, Operator operator,CanonicalTerm right) implements CanonicalTerm, BinaryTerm {}
    record CanonicalUnaryDivTerm(CanonicalTerm term) implements CanonicalTerm, UnaryDivTerm {}
    record MixedComponentTerm(Component component) implements MixedTerm, ComponentTerm {}
    record MixedAnnotTerm(Term term, Annotation annotation) implements MixedTerm, AnnotTerm {}
    record MixedParenTerm(Term term) implements MixedTerm, ParenTerm {}
    record MixedBinaryTerm(Term left, Operator operator, Term right) implements MixedTerm, BinaryTerm {}
    record MixedUnaryDivTerm(Term term) implements MixedTerm, UnaryDivTerm {}

    enum Operator implements UCUMExpression {
        MUL, DIV
    }
}
