package me.fhnau.org.builders;

import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;

public class SoloTermBuilder {

    public static final UCUMExpression.Term UNITY = builder().withIntegerUnit(1).noExpNoAnnot().asTerm().build();

    public interface FinishStep {
        UCUMExpression.Term build();
    }

    public interface TermStep {
        FinishStep asTerm();
        TermStep asTermWithParens();
    }

    public interface AnnotationStep {
        TermStep withAnnotation(String annotation);
        TermStep withoutAnnotation();
    }

    public interface ComponentStep {
        AnnotationStep withExponent(int exponent);
        AnnotationStep withoutExponent();
    }

    public interface FinishUnitStep {
        ComponentStep asComponent();
        TermStep noExpNoAnnot();
    }

    public interface UnitStep {
        FinishUnitStep withPrefix(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit);
        FinishUnitStep withoutPrefix(UCUMDefinition.UCUMUnit unit);
        FinishUnitStep withIntegerUnit(int value);
        TermStep onlyAnnotation(String annotation);
    }

    public static UnitStep builder() {
        return new Builder();
    }

    private static class Builder implements UnitStep, FinishUnitStep, ComponentStep, AnnotationStep, TermStep, FinishStep {

        private UCUMExpression.Unit unit;
        private UCUMExpression.Component component;
        private UCUMExpression.Term term;

        @Override
        public TermStep withAnnotation(String annotation) {
            UCUMExpression.Annotation ann = new UCUMExpression.Annotation(annotation);
            if(component instanceof UCUMExpression.CanonicalComponent canonicalComponent) {
                this.term = new UCUMExpression.CanonicalAnnotTerm(new UCUMExpression.CanonicalComponentTerm(canonicalComponent), ann);
            }
            else {
                this.term = new UCUMExpression.MixedAnnotTerm(new UCUMExpression.MixedComponentTerm(this.component), ann);
            }
            return this;
        }

        @Override
        public TermStep withoutAnnotation() {
            if(component instanceof UCUMExpression.CanonicalComponent canonicalComponent) {
                this.term = new UCUMExpression.CanonicalComponentTerm(canonicalComponent);
            }
            else {
                this.term = new UCUMExpression.MixedComponentTerm(this.component);
            }
            return this;
        }

        @Override
        public AnnotationStep withExponent(int exponent) {
            UCUMExpression.Exponent exponentObj = new UCUMExpression.Exponent(exponent);
            if(unit instanceof UCUMExpression.CanonicalUnit canonicalUnit) {
                this.component = new UCUMExpression.CanonicalComponentExponent(canonicalUnit, exponentObj);
            }
            else {
                this.component = new UCUMExpression.MixedComponentExponent(unit, exponentObj);
            }
            return this;
        }

        @Override
        public AnnotationStep withoutExponent() {
            if(unit instanceof UCUMExpression.CanonicalUnit canonicalUnit) {
                this.component = new UCUMExpression.CanonicalComponentNoExponent(canonicalUnit);
            }
            else {
                this.component = new UCUMExpression.MixedComponentNoExponent(unit);
            }
            return this;
        }

        @Override
        public UCUMExpression.Term build() {
            return this.term;
        }

        @Override
        public ComponentStep asComponent() {
            return this;
        }

        @Override
        public TermStep noExpNoAnnot() {
            this.term = switch(this.unit) {
                    case UCUMExpression.CanonicalUnit canonicalUnit -> new UCUMExpression.CanonicalComponentTerm(new UCUMExpression.CanonicalComponentNoExponent(canonicalUnit));
                    case UCUMExpression.MixedUnit mixedUnit -> new UCUMExpression.MixedComponentTerm(new UCUMExpression.MixedComponentNoExponent(mixedUnit));
                    default -> throw new RuntimeException("REMOVE");
            };
            return this;
        }

        @Override
        public FinishStep asTerm() {
            if(this.term == null) {
                throw new IllegalStateException("Term is null. The contract of the step-builder does not allow a null term here.");
            }
            return this;
        }

        @Override
        public TermStep asTermWithParens() {
            this.term = switch(this.term) {
                case UCUMExpression.MixedTerm mixedTerm -> new UCUMExpression.MixedParenTerm(mixedTerm);
                case UCUMExpression.CanonicalTerm canonicalTerm -> new UCUMExpression.CanonicalParenTerm(canonicalTerm);
                case null -> throw new IllegalStateException("Term is null. The contract of the step-builder does not allow a null term here.");
            };
            return this;
        }

        @Override
        public FinishUnitStep withPrefix(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) {
            this.unit = switch(unit) {
                case UCUMDefinition.BaseUnit baseUnit -> new UCUMExpression.CanonicalPrefixSimpleUnit(prefix, baseUnit);
                case UCUMDefinition.DefinedUnit definedUnit -> new UCUMExpression.MixedPrefixSimpleUnit(prefix, definedUnit);
            };
            return this;
        }

        @Override
        public FinishUnitStep withoutPrefix(UCUMDefinition.UCUMUnit unit) {
            this.unit = switch(unit) {
                case UCUMDefinition.BaseUnit baseUnit -> new UCUMExpression.CanonicalNoPrefixSimpleUnit(baseUnit);
                case UCUMDefinition.DefinedUnit definedUnit -> new UCUMExpression.MixedNoPrefixSimpleUnit(definedUnit);
            };
            return this;
        }

        @Override
        public FinishUnitStep withIntegerUnit(int value) {
            this.unit = new UCUMExpression.IntegerUnit(value);
            return this;
        }

        @Override
        public TermStep onlyAnnotation(String annotation) {
            this.term = new UCUMExpression.AnnotOnlyTerm(new UCUMExpression.Annotation(annotation));
            return this;
        }
    }
}
