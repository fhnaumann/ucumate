package io.github.fhnaumann.model;

import io.github.fhnaumann.NewUCUMBaseVisitor;
import io.github.fhnaumann.NewUCUMParser;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.PrinterService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService.ParserException;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression.*;
import io.github.fhnaumann.util.IUCUMRegistry;
import io.github.fhnaumann.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.fhnaumann.util.SyntaxVisitorHelper.*;

public class UCUMSyntaxVisitor extends NewUCUMBaseVisitor<UCUMExpression> {

    private static final Logger log = LoggerFactory.getLogger(UCUMSyntaxVisitor.class);
    private final IUCUMRegistry registry;

    private final Validator validator = new Validator();
    private final PrinterService printerService = new Printer(validator);

    public UCUMSyntaxVisitor(IUCUMRegistry registry) {
        this.registry = registry;
    }

    @Override
    public UCUMExpression visitDigitSymbols(NewUCUMParser.DigitSymbolsContext ctx) {
        String digitsAsText = ParseUtil.asText(ctx.DIGIT_SYMBOL());
        try {
            int number = Integer.parseInt(digitsAsText);
            return new IntegerUnit(number);
        } catch(NumberFormatException e) {
            throw new RuntimeException("ANTLR4 should not have matched a number if it can't be parsed.");
        }
    }

    @Override
    public UCUMExpression visitMaybeAPrefixSymbolUnit(NewUCUMParser.MaybeAPrefixSymbolUnitContext ctx) {
        ParseUtil.MatchResult matchResult = ParseUtil.separatePrefixFromUnit(ctx.getText(), registry);
        return switch(matchResult) {
            case ParseUtil.SuccessNoPrefixUnit(UCUMDefinition.UCUMUnit unit) -> fromUCUMUnit(unit);
            case ParseUtil.SuccessPrefixUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) -> {
                if(!ConfigurationRegistry.get().isEnablePrefixOnNonMetricUnits() && !ParseUtil.isMetric(unit)) {
                    String prefixString = printerService.print(prefix);
                    String unitString = printerService.print(unit);
                    log.warn("Matched prefix={} and unit={} but {} is not metric and prefixes for non-metric units is disabled.\nYou can change the behaviour with the 'ucumate.enablePrefixOnNonMetricUnits' property.", prefixString, unitString, unitString);
                    throw new ParserException("Matched prefix=%s and unit=%s but %s is not metric and prefixes for non-metric units is disabled.".formatted(prefixString, unitString, unitString));
                }
                yield from(prefix, unit);
            }
            case ParseUtil.InvalidResults invalidResults -> throw new ParserException(invalidResults);
            case ParseUtil.FailureResult failureResult -> throw new ParserException(failureResult);
        };
    }

    @Override
    public UCUMExpression visitStigmatizedSymbolUnit(NewUCUMParser.StigmatizedSymbolUnitContext ctx) {
        UCUMDefinition.DefinedUnit definedUnit = registry.getDefinedUnit(ctx.getText()).orElseThrow(() -> new ParserException("'%s' could not be parsed to a stigmatized unit.".formatted(ctx.getText())));
        return new MixedNoPrefixSimpleUnit(definedUnit);
    }

    @Override
    public UCUMExpression visitAnnotation(NewUCUMParser.AnnotationContext ctx) {
        String annotationText = ParseUtil.asText(ctx.withinCbSymbol());
        ParseUtil.checkASCIIRangeForAnnotation(annotationText);
        return new Annotation(annotationText);
    }

    @Override
    public UCUMExpression visitExponentWithExplicitSign(NewUCUMParser.ExponentWithExplicitSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new Exponent(exponent);
    }

    @Override
    public UCUMExpression visitExponentWithoutSign(NewUCUMParser.ExponentWithoutSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new Exponent(exponent);
    }

    @Override
    public UCUMExpression visitNumberUnit(NewUCUMParser.NumberUnitContext ctx) {
        return visit(ctx.digitSymbols());
    }

    @Override
    public UCUMExpression visitComponentOnly(NewUCUMParser.ComponentOnlyContext ctx) {
        Unit unit = (Unit) visit(ctx.simpleSymbolUnit());
        return from(unit);
    }

    @Override
    public UCUMExpression visitComponentWithExponent(NewUCUMParser.ComponentWithExponentContext ctx) {
        Unit unit = (Unit) visit(ctx.simpleSymbolUnit());
        Exponent exponent = (Exponent) visit(ctx.exponent());
        return from(unit, exponent);
    }

    @Override
    public UCUMExpression visitTermOnly(NewUCUMParser.TermOnlyContext ctx) {
        Component component = (Component) visit(ctx.component());
        return from(component);
    }

    @Override
    public UCUMExpression visitTermWithAnnotation(NewUCUMParser.TermWithAnnotationContext ctx) {
        Term term = (Term) visit(ctx.term());
        /*
        if(!(term instanceof UCUMExpression.ComponentTerm componentTerm)) {
            throw new RuntimeException("Term has annotation when its not allowed!");
        }
        */
        Annotation annotation = (Annotation) visit(ctx.annotation());
        return from(term, annotation);
    }

    @Override
    public UCUMExpression visitAnnotationOnly(NewUCUMParser.AnnotationOnlyContext ctx) {
        Annotation annotation = (Annotation) visit(ctx.annotation());
        return new AnnotOnlyTerm(annotation);
    }

    @Override
    public UCUMExpression visitUnaryDivTerm(NewUCUMParser.UnaryDivTermContext ctx) {
        Term term = (Term) visit(ctx.term());
        return fromForUnaryDiv(term);
    }

    @Override
    public UCUMExpression visitBinaryDivTerm(NewUCUMParser.BinaryDivTermContext ctx) {
        Term left = (Term) visit(ctx.term(0));
        Term right = (Term) visit(ctx.term(1));
        return from(left, Operator.DIV, right);
    }

    @Override
    public UCUMExpression visitBinaryMulTerm(NewUCUMParser.BinaryMulTermContext ctx) {
        Term left = (Term) visit(ctx.term(0));
        Term right = (Term) visit(ctx.term(1));
        return from(left, Operator.MUL, right);
    }

    @Override
    public UCUMExpression visitParenthesisedTerm(NewUCUMParser.ParenthesisedTermContext ctx) {
        Term term = (Term) visit(ctx.term());
        return fromForParen(term);
    }

    @Override
    public UCUMExpression visitCompleteMainTerm(NewUCUMParser.CompleteMainTermContext ctx) {
        return visit(ctx.term());
    }
}
