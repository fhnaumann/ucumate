package io.github.fhnaumann.funcs.sorter;

import io.github.fhnaumann.builders.CombineTermBuilder;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class AlphabeticalSorter implements Sorter {

    @Override
    public Comparator<UCUMExpression.CanonicalComponentTerm> sorter() {
        return Comparator.comparing(canonicalComponentTerm -> ((UCUMExpression.CanonicalSimpleUnit) canonicalComponentTerm.component().unit()).ucumUnit().code());
    }
}
