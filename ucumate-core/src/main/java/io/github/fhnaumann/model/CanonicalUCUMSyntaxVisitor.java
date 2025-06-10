package io.github.fhnaumann.model;

import io.github.fhnaumann.NewUCUMBaseVisitor;
import io.github.fhnaumann.NewUCUMParser;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.util.ParseUtil;
import io.github.fhnaumann.util.UCUMRegistry;

/**
 * @author Felix Naumann
 */
public class CanonicalUCUMSyntaxVisitor extends NewUCUMBaseVisitor<UCUMExpression> {
    private final UCUMRegistry registry;

    public CanonicalUCUMSyntaxVisitor(UCUMRegistry registry) {
        this.registry = registry;
    }

    @Override
    public UCUMExpression visitDigitSymbols(NewUCUMParser.DigitSymbolsContext ctx) {
        String digitsAsText = ParseUtil.asText(ctx.DIGIT_SYMBOL());
        try {
            int number = Integer.parseInt(digitsAsText);
            return new UCUMExpression.IntegerUnit(number);
        } catch(NumberFormatException e) {
            throw new RuntimeException("ANTLR4 should not have matched a number if it can't be parsed.");
        }
    }

    @Override
    public UCUMExpression visitMaybeAPrefixSymbolUnit(NewUCUMParser.MaybeAPrefixSymbolUnitContext ctx) {
        ParseUtil.MatchResult matchResult = ParseUtil.separatePrefixFromUnit(ctx.getText(), registry);
        return switch(matchResult) {
            case ParseUtil.SuccessNoPrefixUnit(UCUMDefinition.UCUMUnit unit) -> new UCUMExpression.CanonicalNoPrefixSimpleUnit((UCUMDefinition.BaseUnit) unit);
            case ParseUtil.SuccessPrefixUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) -> new UCUMExpression.CanonicalPrefixSimpleUnit(prefix, (UCUMDefinition.BaseUnit) unit);
            case ParseUtil.InvalidResults invalidResults -> throw new Validator.ParserException(invalidResults);
            case ParseUtil.FailureResult failureResult -> throw new Validator.ParserException(failureResult);
        };
    }

    @Override
    public UCUMExpression visitAnnotation(NewUCUMParser.AnnotationContext ctx) {
        String annotationText = ParseUtil.asText(ctx.withinCbSymbol());
        ParseUtil.checkASCIIRangeForAnnotation(annotationText);
        return new UCUMExpression.Annotation(annotationText);
    }

    @Override
    public UCUMExpression visitExponentWithExplicitSign(NewUCUMParser.ExponentWithExplicitSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new UCUMExpression.Exponent(exponent);
    }

    @Override
    public UCUMExpression visitExponentWithoutSign(NewUCUMParser.ExponentWithoutSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new UCUMExpression.Exponent(exponent);
    }

    @Override
    public UCUMExpression visitNumberUnit(NewUCUMParser.NumberUnitContext ctx) {
        UCUMExpression.IntegerUnit integerUnit = (UCUMExpression.IntegerUnit) visit(ctx.digitSymbols());
        return integerUnit;
    }

    @Override
    public UCUMExpression visitComponentOnly(NewUCUMParser.ComponentOnlyContext ctx) {
        UCUMExpression.CanonicalUnit unit = (UCUMExpression.CanonicalUnit) visit(ctx.simpleSymbolUnit());
        return new UCUMExpression.CanonicalComponentNoExponent(unit);
    }

    @Override
    public UCUMExpression visitComponentWithExponent(NewUCUMParser.ComponentWithExponentContext ctx) {
        UCUMExpression.CanonicalUnit unit = (UCUMExpression.CanonicalUnit) visit(ctx.simpleSymbolUnit());
        UCUMExpression.Exponent exponent = (UCUMExpression.Exponent) visit(ctx.exponent());
        return new UCUMExpression.CanonicalComponentExponent(unit, exponent);
    }

    @Override
    public UCUMExpression visitTermOnly(NewUCUMParser.TermOnlyContext ctx) {
        UCUMExpression.CanonicalComponent component = (UCUMExpression.CanonicalComponent) visit(ctx.component());
        return new UCUMExpression.CanonicalComponentTerm(component);
    }

    @Override
    public UCUMExpression visitTermWithAnnotation(NewUCUMParser.TermWithAnnotationContext ctx) {
        UCUMExpression.CanonicalTerm term = (UCUMExpression.CanonicalTerm) visit(ctx.term());
        /*
        if(!(term instanceof UCUMExpression.ComponentTerm componentTerm)) {
            throw new RuntimeException("Term has annotation when its not allowed!");
        }
        */
        UCUMExpression.Annotation annotation = (UCUMExpression.Annotation) visit(ctx.annotation());
        return new UCUMExpression.CanonicalAnnotTerm(term, annotation);
    }

    @Override
    public UCUMExpression visitAnnotationOnly(NewUCUMParser.AnnotationOnlyContext ctx) {
        UCUMExpression.Annotation annotation = (UCUMExpression.Annotation) visit(ctx.annotation());
        return new UCUMExpression.AnnotOnlyTerm(annotation);
    }

    @Override
    public UCUMExpression visitUnaryDivTerm(NewUCUMParser.UnaryDivTermContext ctx) {
        UCUMExpression.CanonicalTerm term = (UCUMExpression.CanonicalTerm) visit(ctx.term());
        return new UCUMExpression.CanonicalUnaryDivTerm(term);
    }

    @Override
    public UCUMExpression visitBinaryDivTerm(NewUCUMParser.BinaryDivTermContext ctx) {
        UCUMExpression.CanonicalTerm left = (UCUMExpression.CanonicalTerm) visit(ctx.term(0));
        UCUMExpression.CanonicalTerm right = (UCUMExpression.CanonicalTerm) visit(ctx.term(1));
        return new UCUMExpression.CanonicalBinaryTerm(left, UCUMExpression.Operator.DIV, right);
    }

    @Override
    public UCUMExpression visitBinaryMulTerm(NewUCUMParser.BinaryMulTermContext ctx) {
        UCUMExpression.CanonicalTerm left = (UCUMExpression.CanonicalTerm) visit(ctx.term(0));
        UCUMExpression.CanonicalTerm right = (UCUMExpression.CanonicalTerm) visit(ctx.term(1));
        return new UCUMExpression.CanonicalBinaryTerm(left, UCUMExpression.Operator.MUL, right);
    }

    @Override
    public UCUMExpression visitParenthesisedTerm(NewUCUMParser.ParenthesisedTermContext ctx) {
        UCUMExpression.CanonicalTerm term = (UCUMExpression.CanonicalTerm) visit(ctx.term());
        return new UCUMExpression.CanonicalParenTerm(term);
    }

    @Override
    public UCUMExpression visitCompleteMainTerm(NewUCUMParser.CompleteMainTermContext ctx) {
        return visit(ctx.term());
    }
}

