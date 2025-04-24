/*package org.example;

public class CalcVisitor extends CalculatorBaseVisitor<Integer> {

    @Override
    public Integer visitInt(CalculatorParser.IntContext ctx) {
        int result = Integer.parseInt(ctx.INT().getText());
        if(ctx.SUB() != null) {
            result = -result;
        }
        return result;
    }

    @Override
    public Integer visitParens(CalculatorParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Integer visitAddition(CalculatorParser.AdditionContext ctx) {
        return visit(ctx.expr(0)) + visit(ctx.expr(1));
    }

    @Override
    public Integer visitSubtraction(CalculatorParser.SubtractionContext ctx) {
        return visit(ctx.expr(0)) - visit(ctx.expr(1));
    }

    @Override
    public Integer visitMultiplication(CalculatorParser.MultiplicationContext ctx) {
        return visit(ctx.expr(0)) * visit(ctx.expr(1));
    }

    @Override
    public Integer visitDivision(CalculatorParser.DivisionContext ctx) {
        return visit(ctx.expr(0)) / visit(ctx.expr(1));
    }

    @Override
    public Integer visitPow(CalculatorParser.PowContext ctx) {
        return (int) Math.pow(visit(ctx.expr(0)), visit(ctx.expr(1)));
    }

    @Override
    public Integer visitPrintExpr(CalculatorParser.PrintExprContext ctx) {
        int result = visit(ctx.expr());
        System.out.println(result);
        return 0;
    }
}*/
