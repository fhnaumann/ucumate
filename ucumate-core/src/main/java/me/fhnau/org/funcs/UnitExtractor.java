package me.fhnau.org.funcs;

import me.fhnau.org.UCUMDefinition.*;
import me.fhnau.org.builders.CombineTermBuilder;
import me.fhnau.org.builders.SoloTermBuilder;
import me.fhnau.org.model.UCUMExpression;

public class UnitExtractor {

    public UCUMExpression.Term extractUnits(UCUMExpression.Term term) {
        return switch(term) {
            case UCUMExpression.ComponentTerm componentTerm -> extractUnitsFromUnit(componentTerm.component().unit());
            case UCUMExpression.BinaryTerm binaryTerm -> extractUnitsFromBinaryTerm(binaryTerm);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> CombineTermBuilder.builder().unaryDiv().right(extractUnits(unaryDivTerm.term())).build();
            case UCUMExpression.ParenTerm parenTerm -> extractUnits(parenTerm.term());
            case UCUMExpression.AnnotTerm annotTerm -> extractUnits(annotTerm.term());
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> SoloTermBuilder.UNITY;
        };
    }

    private UCUMExpression.Term extractUnitsFromBinaryTerm(UCUMExpression.BinaryTerm binaryTerm) {
        UCUMExpression.Term leftExtracted = extractUnits(binaryTerm.left());
        UCUMExpression.Term rightExtracted = extractUnits(binaryTerm.right());
        return switch(binaryTerm.operator()) {
            case MUL -> CombineTermBuilder.builder().left(leftExtracted).multiplyWith().right(rightExtracted).build();
            case DIV -> CombineTermBuilder.builder().left(leftExtracted).divideBy().right(rightExtracted).build();
        };
    }

    private static UCUMExpression.Term extractUnitsFromUnit(UCUMExpression.Unit unit) {
        return switch(unit) {
            case UCUMExpression.SimpleUnit simpleUnit -> switch (simpleUnit.ucumUnit()) {
                case BaseUnit baseUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
                case DimlessUnit dimlessUnit -> SoloTermBuilder.UNITY;
                case DerivedUnit derivedUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
                case SpecialUnit specialUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
                case ArbitraryUnit arbitraryUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
            };
            case UCUMExpression.IntegerUnit integerUnit -> SoloTermBuilder.UNITY;
        };
    }
}
