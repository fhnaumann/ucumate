/*package org.example;

import java.util.HashMap;
import java.util.Map;

public class EvalVisitor extends ExprBaseVisitor<Integer> {

    Map<String, Integer> memory = new HashMap<>();
    @Override
    public Integer visitAssign(ExprParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        int value = visit(ctx.expr());
        memory.put(id, value);
        return value;
    }

    @Override
    public Integer visitPrintExpr(ExprParser.PrintExprContext ctx) {
        int value = visit(ctx.expr());
        System.out.println(value);
        return 0;
    }

    @Override
    public Integer visitInt(ExprParser.IntContext ctx) {
        return Integer.valueOf(ctx.INT().getText());
    }

    @Override
    public Integer visitId(ExprParser.IdContext ctx) {
        String id = ctx.ID().getText();
        return memory.computeIfAbsent(id, string -> 0);
    }

    @Override
    public Integer visitParens(ExprParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Integer visitMulDiv(ExprParser.MulDivContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));
        return switch(ctx.op.getType()) {
            case ExprParser.MUL -> left * right;
            case ExprParser.DIV -> left / right;
            default -> throw new RuntimeException();
        };
    }

    @Override
    public Integer visitAddSub(ExprParser.AddSubContext ctx) {
        int left = visit(ctx.expr(0));
        int right = visit(ctx.expr(1));
        return switch(ctx.op.getType()) {
            case ExprParser.ADD -> left + right;
            case ExprParser.SUB -> left - right;
            default -> throw new RuntimeException();
        };
    }

    @Override
    public Integer visitClear(ExprParser.ClearContext ctx) {
        memory.clear();
        return 0; // dummy
    }
}
*/