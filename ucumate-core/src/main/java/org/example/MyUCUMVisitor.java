package org.example;

import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.funcs.PrettyPrinter;
import org.example.model.Expression;
import org.example.util.ParseUtil;

public class MyUCUMVisitor extends NewUCUMBaseVisitor<Expression> {

    private final UCUMRegistry registry;
    private SoloTermBuilder.UnitStep soloTermBuilder;
    private CombineTermBuilder.LeftStep combineTermBuilder;

    public MyUCUMVisitor(UCUMRegistry registry) {
        this.registry = registry;
        this.soloTermBuilder = SoloTermBuilder.builder();
        this.combineTermBuilder = CombineTermBuilder.builder();
    }

    @Override
    public Expression visitDigitSymbols(NewUCUMParser.DigitSymbolsContext ctx) {
        String digitsAsText = ParseUtil.asText(ctx.DIGIT_SYMBOL());
        try {
            int number = Integer.parseInt(digitsAsText);
            return new Expression.IntegerUnit(number);
        } catch(NumberFormatException e) {
            // todo handle error
            return null;
        }
    }

    @Override
    public Expression visitMaybeAPrefixSymbolUnit(NewUCUMParser.MaybeAPrefixSymbolUnitContext ctx) {
        resetSoloTermBuilder();
        ParseUtil.MatchResult matchResult = ParseUtil.separatePrefixFromUnit(ctx.getText(), registry);
        return switch(matchResult) {
            case ParseUtil.SuccessNoPrefixUnit(UCUMDefinition.UCUMUnit unit) -> new Expression.MixedNoPrefixSimpleUnit(unit);
            case ParseUtil.SuccessPrefixUnit(UCUMDefinition.UCUMPrefix prefix, UCUMDefinition.UCUMUnit unit) -> new Expression.MixedPrefixSimpleUnit(prefix, unit);
            default -> throw new RuntimeException(); // todo redo this mess
        };
    }

    @Override
    public Expression visitStigmatizedSymbolUnit(NewUCUMParser.StigmatizedSymbolUnitContext ctx) {
        UCUMDefinition.DefinedUnit definedUnit = registry.getDefinedUnit(ctx.getText()).orElseThrow();
        return new Expression.MixedNoPrefixSimpleUnit(definedUnit);
    }

    @Override
    public Expression visitAnnotation(NewUCUMParser.AnnotationContext ctx) {
        String annotationText = ParseUtil.asText(ctx.withinCbSymbol());
        return new Expression.Annotation(annotationText);
    }

    @Override
    public Expression visitExponentWithExplicitSign(NewUCUMParser.ExponentWithExplicitSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new Expression.Exponent(exponent);
    }

    @Override
    public Expression visitExponentWithoutSign(NewUCUMParser.ExponentWithoutSignContext ctx) {
        int exponent = Integer.parseInt(ctx.getText());
        return new Expression.Exponent(exponent);
    }

    @Override
    public Expression visitNumberUnit(NewUCUMParser.NumberUnitContext ctx) {
        Expression.IntegerUnit integerUnit = (Expression.IntegerUnit) visit(ctx.digitSymbols());
        return integerUnit;
    }

    @Override
    public Expression visitComponentOnly(NewUCUMParser.ComponentOnlyContext ctx) {
        Expression.Unit unit = (Expression.Unit) visit(ctx.simpleSymbolUnit());
        return new Expression.MixedComponentNoExponent(unit);
    }

    @Override
    public Expression visitComponentWithExponent(NewUCUMParser.ComponentWithExponentContext ctx) {
        Expression.Unit unit = (Expression.Unit) visit(ctx.simpleSymbolUnit());
        Expression.Exponent exponent = (Expression.Exponent) visit(ctx.exponent());
        return new Expression.MixedComponentExponent(unit, exponent);
    }

    @Override
    public Expression visitTermOnly(NewUCUMParser.TermOnlyContext ctx) {
        Expression.Component component = (Expression.Component) visit(ctx.component());
        return new Expression.MixedComponentTerm(component);
    }

    @Override
    public Expression visitTermWithAnnotation(NewUCUMParser.TermWithAnnotationContext ctx) {
        Expression.Term term = (Expression.Term) visit(ctx.term());
        if(!(term instanceof Expression.ComponentTerm componentTerm)) {
            throw new RuntimeException("Term has annotation when its not allowed!");
        }
        Expression.Annotation annotation = (Expression.Annotation) visit(ctx.annotation());
        return new Expression.MixedAnnotTerm(componentTerm, annotation);
    }

    @Override
    public Expression visitAnnotationOnly(NewUCUMParser.AnnotationOnlyContext ctx) {
        Expression.Annotation annotation = (Expression.Annotation) visit(ctx.annotation());
        return new Expression.AnnotOnlyTerm(annotation);
    }

    @Override
    public Expression visitUnaryDivTerm(NewUCUMParser.UnaryDivTermContext ctx) {
        Expression.Term term = (Expression.Term) visit(ctx.term());
        return new Expression.MixedUnaryDivTerm(term);
    }

    private SoloTermBuilder.UnitStep resetSoloTermBuilder() {
        soloTermBuilder = SoloTermBuilder.builder();
        return soloTermBuilder;
    }

    private CombineTermBuilder.LeftStep resetCombineTermBuilder() {
        combineTermBuilder = CombineTermBuilder.builder();
        return combineTermBuilder;
    }

    @Override
    public Expression visitBinaryDivTerm(NewUCUMParser.BinaryDivTermContext ctx) {
        resetCombineTermBuilder();

        Expression.Term left = (Expression.Term) visit(ctx.term(0));
        Expression.Term right = (Expression.Term) visit(ctx.term(1));
        return new Expression.MixedBinaryTerm(left, Expression.Operator.DIV, right);
    }

    private static SpecialCheckResult checkForSpecialUnitInTerm(Expression.Term term) {
        // special units may only be multiplied with scalar values
        return switch(term) {
            case Expression.ComponentTerm compTerm -> checkCompTermForSpecialUnitWithExponent(compTerm);
            case Expression.AnnotTerm annotTerm -> checkForSpecialUnitInTerm(annotTerm.term());
            case Expression.AnnotOnlyTerm _ -> SpecialCheckResult.NO_SPECIAL_UNIT_PRESENT; // annot only terms don't have special units (only the unity 1)
            case Expression.ParenTerm parenTerm -> checkForSpecialUnitInTerm(parenTerm.term());
            case Expression.UnaryDivTerm unaryDivTerm -> checkUnaryDivTermForSpecialUnit(unaryDivTerm);
            case Expression.BinaryTerm binaryTerm -> checkBinaryTermForSpecialUnit(binaryTerm);
        };
    }

    private record SpecialCheckResult(boolean isValid, boolean isSpecialUnit) {

        static final SpecialCheckResult NO_SPECIAL_UNIT_PRESENT = new SpecialCheckResult(true, false);

        static SpecialCheckResult and(SpecialCheckResult first, SpecialCheckResult second) {
            boolean valid = first.isValid && second.isValid;
            boolean specialUnit = first.isSpecialUnit && second.isSpecialUnit;
            return new SpecialCheckResult(valid, specialUnit);
        }
    }

    private static SpecialCheckResult checkBinaryTermForSpecialUnit(Expression.BinaryTerm binaryTerm) {
        SpecialCheckResult leftSpecialCheckResult = checkForSpecialUnitInTerm(binaryTerm.left());
        SpecialCheckResult rightSpecialCheckResult = checkForSpecialUnitInTerm(binaryTerm.right());
        boolean bothValid = leftSpecialCheckResult.isValid() && rightSpecialCheckResult.isValid();
        boolean eitherSpecial = leftSpecialCheckResult.isSpecialUnit() || rightSpecialCheckResult.isSpecialUnit();
        return switch(binaryTerm.operator()) {
            case MUL -> new SpecialCheckResult(bothValid, eitherSpecial);
            case DIV -> {
                if(eitherSpecial) {
                    yield new SpecialCheckResult(false, false);
                }
                else {
                    yield new SpecialCheckResult(bothValid, false);
                }
            }
        };
    }

    private static SpecialCheckResult checkUnaryDivTermForSpecialUnit(Expression.UnaryDivTerm unaryDivTerm) {
        SpecialCheckResult specialCheckResult = checkForSpecialUnitInTerm(unaryDivTerm.term());
        if(specialCheckResult.isSpecialUnit()) {
            // special units may not be divided
            return new SpecialCheckResult(false, false);
        }
        else {
            return specialCheckResult;
        }
    }

    private static SpecialCheckResult checkCompTermForSpecialUnitWithExponent(Expression.ComponentTerm compTerm) {
        Expression.Unit unit = compTerm.component().unit();
        return switch(unit) {
            case Expression.IntegerUnit _ -> SpecialCheckResult.NO_SPECIAL_UNIT_PRESENT; // integer units are never special
            // if it's a special unit, make sure it has no exponent
            case Expression.SimpleUnit simpleUnit -> {
                boolean isSpecial = simpleUnit.ucumUnit() instanceof UCUMDefinition.SpecialUnit;
                boolean isValid = !(isSpecial && compTerm.component() instanceof Expression.ComponentExponent);
                yield new SpecialCheckResult(isValid, isSpecial);
            }
        };
    }

    @Override
    public Expression visitBinaryMulTerm(NewUCUMParser.BinaryMulTermContext ctx) {
        Expression.Term left = (Expression.Term) visit(ctx.term(0));
        Expression.Term right = (Expression.Term) visit(ctx.term(1));
        return new Expression.MixedBinaryTerm(left, Expression.Operator.MUL, right);
    }

    @Override
    public Expression visitParenthesisedTerm(NewUCUMParser.ParenthesisedTermContext ctx) {
        Expression.Term term = (Expression.Term) visit(ctx.term());
        return new Expression.MixedParenTerm(term);
    }

    @Override
    public Expression visitCompleteMainTerm(NewUCUMParser.CompleteMainTermContext ctx) {
        Expression.Term term = (Expression.Term) visit(ctx.term());
        boolean valid = checkForSpecialUnitInTerm(term).isValid();
        if(!valid) {
            throw new RuntimeException("TODO: better message, special unit used at invalid place %s".formatted(new PrettyPrinter(false, false, false).print(term)));
        }
        return term;
    }
}
