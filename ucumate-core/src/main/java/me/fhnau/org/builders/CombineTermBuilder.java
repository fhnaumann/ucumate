package me.fhnau.org.builders;

import me.fhnau.org.model.UCUMExpression;

public class CombineTermBuilder {
    public interface FinishStep {
        UCUMExpression.Term build();
        UCUMExpression.CanonicalTerm buildCanonical();
    }

    public interface RightStep {
        FinishStep right(UCUMExpression.Term right);
    }
    public interface OperatorStep {
        RightStep multiplyWith();
        RightStep divideBy();
    }
    public interface LeftStep {
        OperatorStep left(UCUMExpression.Term left);
        RightStep unaryDiv();
    }

    public static LeftStep builder() {
        return new Builder();
    }

    private static class Builder implements FinishStep, RightStep, OperatorStep, LeftStep {

        private UCUMExpression.Term left;
        private UCUMExpression.Term right;
        private UCUMExpression.Operator operator;

        @Override
        public UCUMExpression.Term build() {
            if(left != null && right != null) {
                if(left instanceof UCUMExpression.CanonicalTerm leftCanonicalTerm && right instanceof UCUMExpression.CanonicalTerm rightCanonicalTerm) {
                    return new UCUMExpression.CanonicalBinaryTerm(leftCanonicalTerm, operator, rightCanonicalTerm);
                }
                else {
                    return new UCUMExpression.MixedBinaryTerm(left, operator, right);
                }
            }
            else if(left == null && operator == UCUMExpression.Operator.DIV && right != null) {
                return switch(right) {
                    case UCUMExpression.CanonicalTerm rightCanonicalTerm -> new UCUMExpression.CanonicalUnaryDivTerm(rightCanonicalTerm);
                    case UCUMExpression.MixedTerm mixedTerm -> new UCUMExpression.MixedUnaryDivTerm(mixedTerm);
                };
            }
            throw new RuntimeException("Builder reached unexpected stage.");
        }

        @Override
        public UCUMExpression.CanonicalTerm buildCanonical() {
            try {
                return (UCUMExpression.CanonicalTerm) build();
            } catch (ClassCastException e) {
                throw new RuntimeException("Builder was directed to build a canonical term but the contents given were mixed!");
            }
        }

        @Override
        public OperatorStep left(UCUMExpression.Term left) {
            this.left = handlePotentialParensWrapping(operator, left);
            return this;
        }

        @Override
        public RightStep unaryDiv() {
            this.operator = UCUMExpression.Operator.DIV;
            return this;
        }

        @Override
        public RightStep multiplyWith() {
            this.operator = UCUMExpression.Operator.MUL;
            return this;
        }

        @Override
        public RightStep divideBy() {
            this.operator = UCUMExpression.Operator.DIV;
            return this;
        }

        @Override
        public FinishStep right(UCUMExpression.Term right) {
            this.right = handlePotentialParensWrapping(operator, right);
            return this;
        }

        private UCUMExpression.Term handlePotentialParensWrapping(UCUMExpression.Operator operator, UCUMExpression.Term term) {
            return switch(term) {
                case UCUMExpression.ComponentTerm componentTerm -> term;
                case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> term;
                case UCUMExpression.AnnotTerm annotTerm -> term;
                case UCUMExpression.ParenTerm parenTerm -> term;
                case UCUMExpression.BinaryTerm binaryTerm -> {
                    if(operator == UCUMExpression.Operator.DIV) {
                        yield wrapInParens(binaryTerm);
                    }
                    else {
                        yield term;
                    }
                }
                case UCUMExpression.UnaryDivTerm unaryDivTerm -> {
                    if(operator == UCUMExpression.Operator.DIV) {
                        yield wrapInParens(unaryDivTerm);
                    }
                    else {
                        yield term;
                    }
                }
            };
        }

        private UCUMExpression.Term wrapInParens(UCUMExpression.Term term) {
            return switch(term) {
                case UCUMExpression.CanonicalTerm canonicalTerm -> new UCUMExpression.CanonicalParenTerm(canonicalTerm);
                case UCUMExpression.MixedTerm mixedTerm -> new UCUMExpression.MixedParenTerm(mixedTerm);
            };
        }
    }
}
