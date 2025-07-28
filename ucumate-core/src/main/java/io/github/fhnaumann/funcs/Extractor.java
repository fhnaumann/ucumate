package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class Extractor {

    private final UCUMService service;

    public Extractor(UCUMService service) {
        this.service = service;
    }

    public <R> List<R> extractFrom(UCUMExpression.Term term, Function<UCUMDefinition.UCUMUnit, R> extractor) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> extractFromUnit(componentTerm.component().unit(), extractor);
            case UCUMExpression.AnnotTerm annotTerm -> extractFrom(annotTerm.term(), extractor);
            case UCUMExpression.ParenTerm parenTerm -> extractFrom(parenTerm.term(), extractor);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> extractFrom(unaryDivTerm.term(), extractor);
            case UCUMExpression.BinaryTerm binaryTerm -> Stream.concat(extractFrom(binaryTerm.left(), extractor).stream(), extractFrom(binaryTerm.right(), extractor).stream()).toList();
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> List.of();
        };
    }

    private <R> List<R> extractFromUnit(UCUMExpression.Unit unit, Function<UCUMDefinition.UCUMUnit, R> extractor) {
        return switch (unit) {
            case UCUMExpression.IntegerUnit integerUnit -> List.of();
            case UCUMExpression.SimpleUnit simpleUnit -> List.of(extractor.apply(simpleUnit.ucumUnit()));
        };
    }
}
