package me.fhnau.org.model;

import me.fhnau.org.NewUCUMBaseVisitor;
import me.fhnau.org.NewUCUMParser;
import me.fhnau.org.funcs.Validator.ParserException;
import me.fhnau.org.model.UCUMExpression.ComponentExponent;
import me.fhnau.org.model.UCUMExpression.Operator;
import me.fhnau.org.util.ParseUtil;
import me.fhnau.org.util.UCUMRegistry;

public class UCUMSyntaxVisitor extends NewUCUMBaseVisitor<UCUMExpression> {

    private final UCUMRegistry registry;

    public UCUMSyntaxVisitor(UCUMRegistry registry) {
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
            case ParseUtil.SuccessNoPrefixUnit(UCUMDefinition.UCUMUnit unit) -> new UCUMExpression.MixedNoPrefixSimpleUnit(unit);
            case ParseUtil.SuccessPrefixUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) -> new UCUMExpression.MixedPrefixSimpleUnit(prefix, unit);
            case ParseUtil.InvalidResults invalidResults -> throw new ParserException(invalidResults);
            case ParseUtil.FailureResult failureResult -> throw new ParserException(failureResult);
        };
    }

    @Override
    public UCUMExpression visitStigmatizedSymbolUnit(NewUCUMParser.StigmatizedSymbolUnitContext ctx) {
        UCUMDefinition.DefinedUnit definedUnit = registry.getDefinedUnit(ctx.getText()).orElseThrow(() -> new ParserException("'%s' could not be parsed to a stigmatized unit.".formatted(ctx.getText())));
        return new UCUMExpression.MixedNoPrefixSimpleUnit(definedUnit);
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
        UCUMExpression.Unit unit = (UCUMExpression.Unit) visit(ctx.simpleSymbolUnit());
        return new UCUMExpression.MixedComponentNoExponent(unit);
    }

    @Override
    public UCUMExpression visitComponentWithExponent(NewUCUMParser.ComponentWithExponentContext ctx) {
        UCUMExpression.Unit unit = (UCUMExpression.Unit) visit(ctx.simpleSymbolUnit());
        UCUMExpression.Exponent exponent = (UCUMExpression.Exponent) visit(ctx.exponent());
        return new UCUMExpression.MixedComponentExponent(unit, exponent);
    }

    @Override
    public UCUMExpression visitTermOnly(NewUCUMParser.TermOnlyContext ctx) {
        UCUMExpression.Component component = (UCUMExpression.Component) visit(ctx.component());
        return new UCUMExpression.MixedComponentTerm(component);
    }

    @Override
    public UCUMExpression visitTermWithAnnotation(NewUCUMParser.TermWithAnnotationContext ctx) {
        UCUMExpression.Term term = (UCUMExpression.Term) visit(ctx.term());
        /*
        if(!(term instanceof UCUMExpression.ComponentTerm componentTerm)) {
            throw new RuntimeException("Term has annotation when its not allowed!");
        }
        */
        UCUMExpression.Annotation annotation = (UCUMExpression.Annotation) visit(ctx.annotation());
        return new UCUMExpression.MixedAnnotTerm(term, annotation);
    }

    @Override
    public UCUMExpression visitAnnotationOnly(NewUCUMParser.AnnotationOnlyContext ctx) {
        UCUMExpression.Annotation annotation = (UCUMExpression.Annotation) visit(ctx.annotation());
        return new UCUMExpression.AnnotOnlyTerm(annotation);
    }

    @Override
    public UCUMExpression visitUnaryDivTerm(NewUCUMParser.UnaryDivTermContext ctx) {
        UCUMExpression.Term term = (UCUMExpression.Term) visit(ctx.term());
        return new UCUMExpression.MixedUnaryDivTerm(term);
    }

    @Override
    public UCUMExpression visitBinaryDivTerm(NewUCUMParser.BinaryDivTermContext ctx) {
        UCUMExpression.Term left = (UCUMExpression.Term) visit(ctx.term(0));
        UCUMExpression.Term right = (UCUMExpression.Term) visit(ctx.term(1));
        return new UCUMExpression.MixedBinaryTerm(left, Operator.DIV, right);
    }

    @Override
    public UCUMExpression visitBinaryMulTerm(NewUCUMParser.BinaryMulTermContext ctx) {
        UCUMExpression.Term left = (UCUMExpression.Term) visit(ctx.term(0));
        UCUMExpression.Term right = (UCUMExpression.Term) visit(ctx.term(1));
        return new UCUMExpression.MixedBinaryTerm(left, Operator.MUL, right);
    }

    @Override
    public UCUMExpression visitParenthesisedTerm(NewUCUMParser.ParenthesisedTermContext ctx) {
        UCUMExpression.Term term = (UCUMExpression.Term) visit(ctx.term());
        return new UCUMExpression.MixedParenTerm(term);
    }

    @Override
    public UCUMExpression visitCompleteMainTerm(NewUCUMParser.CompleteMainTermContext ctx) {
        return visit(ctx.term());
    }
}
