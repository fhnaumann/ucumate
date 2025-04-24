package org.example.builders;

import org.example.UCUMDefinition;
import org.example.model.Expression;

public class SoloTermBuilder {

    public static final Expression.Term UNITY = builder().withIntegerUnit(1).noExpNoAnnot().asTerm().build();

    public interface FinishStep {
        Expression.Term build();
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

        private Expression.Unit unit;
        private Expression.Component component;
        private Expression.Term term;

        @Override
        public TermStep withAnnotation(String annotation) {
            Expression.Annotation ann = new Expression.Annotation(annotation);
            if(component instanceof Expression.CanonicalComponent canonicalComponent) {
                this.term = new Expression.CanonicalAnnotTerm(new Expression.CanonicalComponentTerm(canonicalComponent), ann);
            }
            else {
                this.term = new Expression.MixedAnnotTerm(new Expression.MixedComponentTerm(this.component), ann);
            }
            return this;
        }

        @Override
        public TermStep withoutAnnotation() {
            if(component instanceof Expression.CanonicalComponent canonicalComponent) {
                this.term = new Expression.CanonicalComponentTerm(canonicalComponent);
            }
            else {
                this.term = new Expression.MixedComponentTerm(this.component);
            }
            return this;
        }

        @Override
        public AnnotationStep withExponent(int exponent) {
            Expression.Exponent exponentObj = new Expression.Exponent(exponent);
            if(unit instanceof Expression.CanonicalUnit canonicalUnit) {
                this.component = new Expression.CanonicalComponentExponent(canonicalUnit, exponentObj);
            }
            else {
                this.component = new Expression.MixedComponentExponent(unit, exponentObj);
            }
            return this;
        }

        @Override
        public AnnotationStep withoutExponent() {
            if(unit instanceof Expression.CanonicalUnit canonicalUnit) {
                this.component = new Expression.CanonicalComponentNoExponent(canonicalUnit);
            }
            else {
                this.component = new Expression.MixedComponentNoExponent(unit);
            }
            return this;
        }

        @Override
        public Expression.Term build() {
            return this.term;
        }

        @Override
        public ComponentStep asComponent() {
            return this;
        }

        @Override
        public TermStep noExpNoAnnot() {
            this.term = switch(this.unit) {
                    case Expression.CanonicalUnit canonicalUnit -> new Expression.CanonicalComponentTerm(new Expression.CanonicalComponentNoExponent(canonicalUnit));
                    case Expression.MixedUnit mixedUnit -> new Expression.MixedComponentTerm(new Expression.MixedComponentNoExponent(mixedUnit));
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
                case Expression.MixedTerm mixedTerm -> new Expression.MixedParenTerm(mixedTerm);
                case Expression.CanonicalTerm canonicalTerm -> new Expression.CanonicalParenTerm(canonicalTerm);
                case null -> throw new IllegalStateException("Term is null. The contract of the step-builder does not allow a null term here.");
            };
            return this;
        }

        @Override
        public FinishUnitStep withPrefix(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) {
            this.unit = switch(unit) {
                case UCUMDefinition.BaseUnit baseUnit -> new Expression.CanonicalPrefixSimpleUnit(prefix, baseUnit);
                case UCUMDefinition.DefinedUnit definedUnit -> new Expression.MixedPrefixSimpleUnit(prefix, definedUnit);
            };
            return this;
        }

        @Override
        public FinishUnitStep withoutPrefix(UCUMDefinition.UCUMUnit unit) {
            this.unit = switch(unit) {
                case UCUMDefinition.BaseUnit baseUnit -> new Expression.CanonicalNoPrefixSimpleUnit(baseUnit);
                case UCUMDefinition.DefinedUnit definedUnit -> new Expression.MixedNoPrefixSimpleUnit(definedUnit);
            };
            return this;
        }

        @Override
        public FinishUnitStep withIntegerUnit(int value) {
            this.unit = new Expression.IntegerUnit(value);
            return this;
        }

        @Override
        public TermStep onlyAnnotation(String annotation) {
            this.term = new Expression.AnnotOnlyTerm(new Expression.Annotation(annotation));
            return this;
        }
    }
}
