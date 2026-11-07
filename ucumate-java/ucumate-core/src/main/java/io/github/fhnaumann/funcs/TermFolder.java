package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class TermFolder {

    private final UCUMService service; //NOSONAR will need it later

    public TermFolder(UCUMService service) {
        this.service = service;
    }

    public <R> R fold(UCUMExpression.Term term, Function<UCUMDefinition.UCUMUnit, R> unitMapper, Supplier<R> integerValue, Supplier<R> annotOnlyValue, BinaryOperator<R> combiner) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> foldUnit(componentTerm.component().unit(), unitMapper, integerValue);
            case UCUMExpression.AnnotTerm annotTerm -> fold(annotTerm.term(), unitMapper, integerValue, annotOnlyValue, combiner);
            case UCUMExpression.ParenTerm parenTerm -> fold(parenTerm.term(), unitMapper, integerValue, annotOnlyValue, combiner);
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> fold(unaryDivTerm.term(), unitMapper, integerValue, annotOnlyValue, combiner);
            case UCUMExpression.BinaryTerm binaryTerm -> combiner.apply(
                    fold(binaryTerm.left(), unitMapper, integerValue, annotOnlyValue, combiner),
                    fold(binaryTerm.right(), unitMapper, integerValue, annotOnlyValue, combiner)
            );
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> annotOnlyValue.get();
        };
    }

    private <R> R foldUnit(UCUMExpression.Unit unit, Function<UCUMDefinition.UCUMUnit, R> unitMapper, Supplier<R> integerValue) {
        return switch (unit) {
            case UCUMExpression.IntegerUnit integerUnit -> integerValue.get();
            case UCUMExpression.SimpleUnit simpleUnit -> unitMapper.apply(simpleUnit.ucumUnit());
        };
    }

    public <R> List<R> extractFrom(UCUMExpression.Term term, Function<UCUMDefinition.UCUMUnit, R> extractor) {
        return fold(
                term,
                unit -> List.of(extractor.apply(unit)),
                List::of,
                List::of,
                listCombiner());
    }

    public boolean predicate(UCUMExpression.Term term, Predicate<UCUMDefinition.UCUMUnit> predicate, boolean valueIfNoUCUMUnit, BinaryOperator<Boolean> combiner) {
        return fold(
                term,
                predicate::test,
                () -> valueIfNoUCUMUnit,
                () -> valueIfNoUCUMUnit,
                combiner
        );
    }

    private static <T> BinaryOperator<List<T>> listCombiner() {
        return (left, right) -> Stream.concat(left.stream(), right.stream()).toList();
    }
}
