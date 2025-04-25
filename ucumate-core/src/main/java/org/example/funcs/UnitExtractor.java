package org.example.funcs;

import org.example.UCUMDefinition;
import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;

import java.util.Set;

public class UnitExtractor {

    public Expression.Term extractUnits(Expression.Term term) {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> extractUnitsFromUnit(componentTerm.component().unit());
            case Expression.BinaryTerm binaryTerm -> extractUnitsFromBinaryTerm(binaryTerm);
            case Expression.UnaryDivTerm unaryDivTerm -> CombineTermBuilder.builder().unaryDiv().right(extractUnits(unaryDivTerm.term())).build();
            case Expression.ParenTerm parenTerm -> extractUnits(parenTerm.term());
            case Expression.AnnotTerm annotTerm -> extractUnits(annotTerm.term());
            case Expression.AnnotOnlyTerm _ -> SoloTermBuilder.UNITY;
        };
    }

    private Expression.Term extractUnitsFromBinaryTerm(Expression.BinaryTerm binaryTerm) {
        Expression.Term leftExtracted = extractUnits(binaryTerm.left());
        Expression.Term rightExtracted = extractUnits(binaryTerm.right());
        return switch(binaryTerm.operator()) {
            case MUL -> CombineTermBuilder.builder().left(leftExtracted).multiplyWith().right(rightExtracted).build();
            case DIV -> CombineTermBuilder.builder().left(leftExtracted).divideBy().right(rightExtracted).build();
        };
    }

    private static Expression.Term extractUnitsFromUnit(Expression.Unit unit) {
        return switch(unit) {
            case Expression.SimpleUnit simpleUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
            case Expression.IntegerUnit _ -> SoloTermBuilder.UNITY;
        };
    }
}
