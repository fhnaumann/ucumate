package io.github.fhnaumann;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.ParseUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Felix Naumann
 */
public class MyFeedbackVisitor extends ErrorFeedbackUCUMBaseVisitor<UCUMExpression> {

    private static final Logger log = LoggerFactory.getLogger(MyFeedbackVisitor.class);
    private final UCUMRegistry registry;

    private final List<String> errorMessages;

    public MyFeedbackVisitor(UCUMRegistry registry, List<String> errorMessages) {
        this.registry = registry;
        this.errorMessages = errorMessages;
    }

    @Override
    public UCUMExpression visitDigitSymbols(ErrorFeedbackUCUMParser.DigitSymbolsContext ctx) {
        String digitsAsText = ParseUtil.asText(ctx.DIGIT_SYMBOL());
        try {
            int number = Integer.parseInt(digitsAsText);
            return new UCUMExpression.IntegerUnit(number);
        } catch(NumberFormatException e) {
            throw new RuntimeException("ANTLR4 should not have matched a number if it can't be parsed.");
        }
    }

    @Override
    public UCUMExpression visitMaybeAPrefixSymbolUnit(ErrorFeedbackUCUMParser.MaybeAPrefixSymbolUnitContext ctx) {
        ParseUtil.MatchResult matchResult = ParseUtil.separatePrefixFromUnit(ctx.getText(), registry);
        return switch(matchResult) {
            case ParseUtil.SuccessNoPrefixUnit(UCUMDefinition.UCUMUnit unit) -> new UCUMExpression.MixedNoPrefixSimpleUnit(unit);
            case ParseUtil.SuccessPrefixUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) -> {
                if(!ConfigurationRegistry.get().isEnablePrefixOnNonMetricUnits() && !ParseUtil.isMetric(unit)) {
                    String prefixString = UCUMService.print(prefix);
                    String unitString = UCUMService.print(unit);
                    log.warn("Matched prefix={} and unit={} but {} is not metric and prefixes for non-metric units is disabled.\nYou can change the behaviour with the 'ucumate.enablePrefixOnNonMetricUnits' property.", prefixString, unitString, unitString);
                    throw new Validator.ParserException("Matched prefix=%s and unit=%s but %s is not metric and prefixes for non-metric units is disabled.".formatted(prefixString, unitString, unitString));
                }
                yield new UCUMExpression.MixedPrefixSimpleUnit(prefix, unit);
            }
            case ParseUtil.InvalidResults invalidResults -> throw new Validator.ParserException(invalidResults);
            case ParseUtil.FailureResult failureResult -> throw new Validator.ParserException(failureResult);
        };
    }

    @Override
    public UCUMExpression visitStigmatizedSymbolUnit(ErrorFeedbackUCUMParser.StigmatizedSymbolUnitContext ctx) {
        UCUMDefinition.DefinedUnit definedUnit = registry.getDefinedUnit(ctx.getText()).orElseThrow(() -> new Validator.ParserException("'%s' could not be parsed to a stigmatized unit.".formatted(ctx.getText())));
        return new UCUMExpression.MixedNoPrefixSimpleUnit(definedUnit);
    }

    @Override
    public UCUMExpression visitAnnotation(ErrorFeedbackUCUMParser.AnnotationContext ctx) {
        String annotationText = ParseUtil.asText(ctx.withinCbSymbol());
        ParseUtil.checkASCIIRangeForAnnotation(annotationText);
        return new UCUMExpression.Annotation(annotationText);
    }

    @Override
    public UCUMExpression visitExponentWithExplicitSign(ErrorFeedbackUCUMParser.ExponentWithExplicitSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new UCUMExpression.Exponent(exponent);
    }

    @Override
    public UCUMExpression visitExponentWithoutSign(ErrorFeedbackUCUMParser.ExponentWithoutSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new UCUMExpression.Exponent(exponent);
    }

    @Override
    public UCUMExpression visitNumberUnit(ErrorFeedbackUCUMParser.NumberUnitContext ctx) {
        UCUMExpression.IntegerUnit integerUnit = (UCUMExpression.IntegerUnit) visit(ctx.digitSymbols());
        return integerUnit;
    }

    @Override
    public UCUMExpression visitComponentOnly(ErrorFeedbackUCUMParser.ComponentOnlyContext ctx) {
        UCUMExpression.Unit unit = (UCUMExpression.Unit) visit(ctx.simpleSymbolUnit());
        return new UCUMExpression.MixedComponentNoExponent(unit);
    }

    @Override
    public UCUMExpression visitComponentWithExponent(ErrorFeedbackUCUMParser.ComponentWithExponentContext ctx) {
        UCUMExpression.Unit unit = (UCUMExpression.Unit) visit(ctx.simpleSymbolUnit());
        UCUMExpression.Exponent exponent = (UCUMExpression.Exponent) visit(ctx.exponent());
        return new UCUMExpression.MixedComponentExponent(unit, exponent);
    }

    @Override
    public UCUMExpression visitTermOnly(ErrorFeedbackUCUMParser.TermOnlyContext ctx) {
        UCUMExpression.Component component = (UCUMExpression.Component) visit(ctx.component());
        return new UCUMExpression.MixedComponentTerm(component);
    }

    @Override
    public UCUMExpression visitTermWithAnnotation(ErrorFeedbackUCUMParser.TermWithAnnotationContext ctx) {
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
    public UCUMExpression visitAnnotationOnly(ErrorFeedbackUCUMParser.AnnotationOnlyContext ctx) {
        UCUMExpression.Annotation annotation = (UCUMExpression.Annotation) visit(ctx.annotation());
        return new UCUMExpression.AnnotOnlyTerm(annotation);
    }

    @Override
    public UCUMExpression visitUnaryDivTerm(ErrorFeedbackUCUMParser.UnaryDivTermContext ctx) {
        UCUMExpression.Term term = (UCUMExpression.Term) visit(ctx.term());
        return new UCUMExpression.MixedUnaryDivTerm(term);
    }

    @Override
    public UCUMExpression visitBinaryDivTerm(ErrorFeedbackUCUMParser.BinaryDivTermContext ctx) {
        String divSymbol = ctx.getChild(1).getText();
        SyntaxMatchHelper.checkWrongButKnownDivSymbolUsed(divSymbol, errorMessages);
        UCUMExpression.Term left = (UCUMExpression.Term) visit(ctx.term(0));
        UCUMExpression.Term right = (UCUMExpression.Term) visit(ctx.term(1));
        return new UCUMExpression.MixedBinaryTerm(left, UCUMExpression.Operator.DIV, right);
    }

    @Override
    public UCUMExpression visitBinaryMulTerm(ErrorFeedbackUCUMParser.BinaryMulTermContext ctx) {
        String mulSymbol = ctx.getChild(1).getText();
        SyntaxMatchHelper.checkWrongButKnownMulSymbolUsed(mulSymbol, errorMessages);
        UCUMExpression.Term left = (UCUMExpression.Term) visit(ctx.term(0));
        UCUMExpression.Term right = (UCUMExpression.Term) visit(ctx.term(1));
        return new UCUMExpression.MixedBinaryTerm(left, UCUMExpression.Operator.MUL, right);
    }

    @Override
    public UCUMExpression visitParenthesisedTerm(ErrorFeedbackUCUMParser.ParenthesisedTermContext ctx) {
        UCUMExpression.Term term = (UCUMExpression.Term) visit(ctx.term());
        return new UCUMExpression.MixedParenTerm(term);
    }

    @Override
    public UCUMExpression visitEmptyMainTerm(ErrorFeedbackUCUMParser.EmptyMainTermContext ctx) {
        return super.visitEmptyMainTerm(ctx);
    }

    @Override
    public UCUMExpression visitCompleteMainTerm(ErrorFeedbackUCUMParser.CompleteMainTermContext ctx) {
        return visit(ctx.term());
    }

    @Override
    public UCUMExpression visitMissingLHS(ErrorFeedbackUCUMParser.MissingLHSContext ctx) {
        errorMessages.add(ErrorMessages.get("binary_term_missing_lhs", ctx.getChild(1).getText()));
        SyntaxMatchHelper.checkWrongButKnownMulSymbolUsed(ctx.getChild(0).getText(), errorMessages);
        SyntaxMatchHelper.checkWrongButKnownDivSymbolUsed(ctx.getChild(0).getText(), errorMessages);
        return visit(ctx.term());
    }

    @Override
    public UCUMExpression visitMissingRHS(ErrorFeedbackUCUMParser.MissingRHSContext ctx) {
        errorMessages.add(ErrorMessages.get("binary_term_missing_rhs", ctx.getChild(0).getText()));
        SyntaxMatchHelper.checkWrongButKnownMulSymbolUsed(ctx.getChild(1).getText(), errorMessages);
        SyntaxMatchHelper.checkWrongButKnownDivSymbolUsed(ctx.getChild(1).getText(), errorMessages);
        return visit(ctx.term());
    }

    @Override
    public UCUMExpression visitMissingLeftParen(ErrorFeedbackUCUMParser.MissingLeftParenContext ctx) {
        //errorMessages.addAll(ErrorMessages.get("binary_term_missing_left_paren", ))
        return super.visitMissingLeftParen(ctx);
    }

    @Override
    public UCUMExpression visitMissingRightParen(ErrorFeedbackUCUMParser.MissingRightParenContext ctx) {
        return super.visitMissingRightParen(ctx);
    }

    @Override
    public UCUMExpression visitInvalidNumberUnit(ErrorFeedbackUCUMParser.InvalidNumberUnitContext ctx) {
        // only negative numbers for now
        errorMessages.add(ErrorMessages.get("negative_number", ctx.getText()));
        return super.visitInvalidNumberUnit(ctx);
    }

    @Override
    public UCUMExpression visitStigmatizedSymbolUnitMissingClosingSquareBracket(ErrorFeedbackUCUMParser.StigmatizedSymbolUnitMissingClosingSquareBracketContext ctx) {
        // Will be checked earlier by SyntaxMatchHelper#searchForAnyUnbalancedParens
        //errorMessages.add(ErrorMessages.get("missing_right_square_bracket", ctx.getText()));
        return super.visitStigmatizedSymbolUnitMissingClosingSquareBracket(ctx);
    }

    @Override
    public UCUMExpression visitStigmatizedSymbolunitMissingOpeningSquareBracket(ErrorFeedbackUCUMParser.StigmatizedSymbolunitMissingOpeningSquareBracketContext ctx) {
        // Will be checked earlier by SyntaxMatchHelper#searchForAnyUnbalancedParens
        //errorMessages.add(ErrorMessages.get("missing_left_square_bracket", ctx.getText()));
        return super.visitStigmatizedSymbolunitMissingOpeningSquareBracket(ctx);
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
