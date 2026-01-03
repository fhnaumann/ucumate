package io.github.fhnaumann;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.util.ParseUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.github.fhnaumann.model.UCUMExpression.*;
import static io.github.fhnaumann.util.SyntaxVisitorHelper.*;

/**
 * @author Felix Naumann
 */
public class MyFeedbackVisitor extends ErrorFeedbackUCUMBaseVisitor<UCUMExpression> {

    private static final Logger log = LoggerFactory.getLogger(MyFeedbackVisitor.class);
    private final VersionSpecificUCUMRegistry registry;

    private final List<String> errorMessages;

    private final Printer printer = new Printer();

    public MyFeedbackVisitor(UcumVersion version, UCUMRegistry registry, List<String> errorMessages) {
        this.registry = registry.getVersionSpecificUCUMRegistry(version);
        this.errorMessages = errorMessages;
    }

    @Override
    public UCUMExpression visitDigitSymbols(ErrorFeedbackUCUMParser.DigitSymbolsContext ctx) {
        String digitsAsText = ParseUtil.asText(ctx.DIGIT_SYMBOL());
        try {
            int number = Integer.parseInt(digitsAsText);
            return new IntegerUnit(number);
        } catch(NumberFormatException e) {
            throw new IllegalStateException("ANTLR4 should not have matched a number if it can't be parsed.");
        }
    }

    @Override
    public UCUMExpression visitMaybeAPrefixSymbolUnit(ErrorFeedbackUCUMParser.MaybeAPrefixSymbolUnitContext ctx) {
        ParseUtil.MatchResult matchResult = ParseUtil.separatePrefixFromUnit(ctx.getText(), registry);
        return switch(matchResult) {
            case ParseUtil.SuccessNoPrefixUnit(UCUMDefinition.UCUMUnit unit) -> fromUCUMUnit(unit);
            case ParseUtil.SuccessPrefixUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) -> {
                if(!ConfigurationRegistry.get().isEnablePrefixOnNonMetricUnits() && !ParseUtil.isMetric(unit)) {
                    String prefixString = printer.print(prefix);
                    String unitString = printer.print(unit);
                    log.warn("Matched prefix={} and unit={} but {} is not metric and prefixes for non-metric units is disabled.\nYou can change the behaviour with the 'ucumate.enablePrefixOnNonMetricUnits' property.", prefixString, unitString, unitString);
                    throw new Validator.ParserException("Matched prefix=%s and unit=%s but %s is not metric and prefixes for non-metric units is disabled.".formatted(prefixString, unitString, unitString));
                }
                yield from(prefix, unit);
            }
            case ParseUtil.InvalidResults invalidResults -> throw new Validator.ParserException(invalidResults);
            case ParseUtil.FailureResult failureResult -> throw new Validator.ParserException(failureResult);
        };
    }

    @Override
    public UCUMExpression visitStigmatizedSymbolUnit(ErrorFeedbackUCUMParser.StigmatizedSymbolUnitContext ctx) {
        UCUMDefinition.DefinedUnit definedUnit = registry.getDefinedUnit(ctx.getText()).orElseThrow(() -> new Validator.ParserException("'%s' could not be parsed to a stigmatized unit.".formatted(ctx.getText())));
        return new MixedNoPrefixSimpleUnit(definedUnit);
    }

    @Override
    public UCUMExpression visitAnnotation(ErrorFeedbackUCUMParser.AnnotationContext ctx) {
        String annotationText = ctx.getText();
        try {
            ParseUtil.checkASCIIRangeForAnnotation(annotationText);
        } catch (ValidatorService.ParserException e) {
            if("nesting".equals(e.getMessage())) {
                errorMessages.add(ErrorMessages.get("nested_annotations"));
            }
        }
        return new Annotation(annotationText);
    }

    @Override
    public UCUMExpression visitExponentWithExplicitSign(ErrorFeedbackUCUMParser.ExponentWithExplicitSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new Exponent(exponent);
    }

    @Override
    public UCUMExpression visitExponentWithoutSign(ErrorFeedbackUCUMParser.ExponentWithoutSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new Exponent(exponent);
    }

    @Override
    public UCUMExpression visitNumberUnit(ErrorFeedbackUCUMParser.NumberUnitContext ctx) {
        return visit(ctx.digitSymbols());
    }

    @Override
    public UCUMExpression visitComponentOnly(ErrorFeedbackUCUMParser.ComponentOnlyContext ctx) {
        Unit unit = (Unit) visit(ctx.simpleSymbolUnit());
        return from(unit);
    }

    @Override
    public UCUMExpression visitComponentWithExponent(ErrorFeedbackUCUMParser.ComponentWithExponentContext ctx) {
        String exponentSymbol = ctx.getChild(1).getText();
        SyntaxMatchHelper.checkWrongButKnownExpSymbolUsed(exponentSymbol, errorMessages);
        Unit unit = (Unit) visit(ctx.simpleSymbolUnit());
        Exponent exponent = (Exponent) visit(ctx.exponent());
        return from(unit, exponent);
    }

    @Override
    public UCUMExpression visitTermOnly(ErrorFeedbackUCUMParser.TermOnlyContext ctx) {
        Component component = (Component) visit(ctx.component());
        return from(component);
    }

    @Override
    public UCUMExpression visitTermWithAnnotation(ErrorFeedbackUCUMParser.TermWithAnnotationContext ctx) {
        Term term = (Term) visit(ctx.term());
        Annotation annotation = (Annotation) visit(ctx.annotation());
        return from(term, annotation);
    }

    @Override
    public UCUMExpression visitAnnotationOnly(ErrorFeedbackUCUMParser.AnnotationOnlyContext ctx) {
        Annotation annotation = (Annotation) visit(ctx.annotation());
        return new AnnotOnlyTerm(annotation);
    }

    @Override
    public UCUMExpression visitUnaryDivTerm(ErrorFeedbackUCUMParser.UnaryDivTermContext ctx) {
        Term term = (Term) visit(ctx.term());
        return fromForUnaryDiv(term);
    }

    @Override
    public UCUMExpression visitBinaryDivTerm(ErrorFeedbackUCUMParser.BinaryDivTermContext ctx) {
        String divSymbol = ctx.getChild(1).getText();
        SyntaxMatchHelper.checkWrongButKnownDivSymbolUsed(divSymbol, errorMessages);
        Term left = (Term) visit(ctx.term(0));
        Term right = (Term) visit(ctx.term(1));
        return from(left, Operator.DIV, right);
    }

    @Override
    public UCUMExpression visitBinaryMulTerm(ErrorFeedbackUCUMParser.BinaryMulTermContext ctx) {
        String mulSymbol = ctx.getChild(1).getText();
        SyntaxMatchHelper.checkWrongButKnownMulSymbolUsed(mulSymbol, errorMessages);
        Term left = (Term) visit(ctx.term(0));
        Term right = (Term) visit(ctx.term(1));
        return from(left, Operator.MUL, right);
    }

    @Override
    public UCUMExpression visitParenthesisedTerm(ErrorFeedbackUCUMParser.ParenthesisedTermContext ctx) {
        Term term = (Term) visit(ctx.term());
        return fromForParen(term);
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
    public UCUMExpression visitInvalidNumberUnit(ErrorFeedbackUCUMParser.InvalidNumberUnitContext ctx) {
        // only negative numbers for now
        errorMessages.add(ErrorMessages.get("negative_number", ctx.getText()));
        return super.visitInvalidNumberUnit(ctx);
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
