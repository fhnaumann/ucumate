package org.example.builders;

import org.example.model.Expression;

public class CombineTermBuilder {
    public interface FinishStep {
        Expression.Term build();
        Expression.CanonicalTerm buildCanonical();
    }

    public interface RightStep {
        FinishStep right(Expression.Term right);
    }
    public interface OperatorStep {
        RightStep multiplyWith();
        RightStep divideBy();
    }
    public interface LeftStep {
        OperatorStep left(Expression.Term left);
        RightStep unaryDiv();
    }

    public static LeftStep builder() {
        return new Builder();
    }

    private static class Builder implements FinishStep, RightStep, OperatorStep, LeftStep {

        private Expression.Term left;
        private Expression.Term right;
        private Expression.Operator operator;

        @Override
        public Expression.Term build() {
            if(left != null && right != null) {
                if(left instanceof Expression.CanonicalTerm leftCanonicalTerm && right instanceof Expression.CanonicalTerm rightCanonicalTerm) {
                    return new Expression.CanonicalBinaryTerm(leftCanonicalTerm, operator, rightCanonicalTerm);
                }
                else {
                    return new Expression.MixedBinaryTerm(left, operator, right);
                }
            }
            else if(left == null && operator == Expression.Operator.DIV && right != null) {
                return switch(right) {
                    case Expression.CanonicalTerm rightCanonicalTerm -> new Expression.CanonicalUnaryDivTerm(rightCanonicalTerm);
                    case Expression.MixedTerm mixedTerm -> new Expression.MixedUnaryDivTerm(mixedTerm);
                };
            }
            throw new RuntimeException("Builder reached unexpected stage.");
        }

        @Override
        public Expression.CanonicalTerm buildCanonical() {
            try {
                return (Expression.CanonicalTerm) build();
            } catch (ClassCastException e) {
                throw new RuntimeException("Builder was directed to build a canonical term but the contents given were mixed!");
            }
        }

        @Override
        public OperatorStep left(Expression.Term left) {
            this.left = handlePotentialParensWrapping(operator, left);
            return this;
        }

        @Override
        public RightStep unaryDiv() {
            this.operator = Expression.Operator.DIV;
            return this;
        }

        @Override
        public RightStep multiplyWith() {
            this.operator = Expression.Operator.MUL;
            return this;
        }

        @Override
        public RightStep divideBy() {
            this.operator = Expression.Operator.DIV;
            return this;
        }

        @Override
        public FinishStep right(Expression.Term right) {
            this.right = handlePotentialParensWrapping(operator, right);
            return this;
        }

        private Expression.Term handlePotentialParensWrapping(Expression.Operator operator, Expression.Term term) {
            return switch(term) {
                case Expression.ComponentTerm _, Expression.AnnotOnlyTerm _, Expression.AnnotTerm _,
                     Expression.ParenTerm _ -> term;
                case Expression.BinaryTerm binaryTerm -> {
                    if(operator == Expression.Operator.DIV) {
                        yield wrapInParens(binaryTerm);
                    }
                    else {
                        yield term;
                    }
                }
                case Expression.UnaryDivTerm unaryDivTerm -> {
                    if(operator == Expression.Operator.DIV) {
                        yield wrapInParens(unaryDivTerm);
                    }
                    else {
                        yield term;
                    }
                }
            };
        }

        private Expression.Term wrapInParens(Expression.Term term) {
            return switch(term) {
                case Expression.CanonicalTerm canonicalTerm -> new Expression.CanonicalParenTerm(canonicalTerm);
                case Expression.MixedTerm mixedTerm -> new Expression.MixedParenTerm(mixedTerm);
            };
        }
    }
}
