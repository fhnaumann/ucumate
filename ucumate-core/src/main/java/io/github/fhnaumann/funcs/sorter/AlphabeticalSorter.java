package io.github.fhnaumann.funcs.sorter;

import io.github.fhnaumann.model.UCUMExpression;

import java.util.Comparator;

/**
 * @author Felix Naumann
 */
public class AlphabeticalSorter implements Sorter {

    @Override
    public Comparator<UCUMExpression.CanonicalComponentTerm> sorter() {
        return Comparator.comparing(canonicalComponentTerm -> ((UCUMExpression.CanonicalSimpleUnit) canonicalComponentTerm.component().unit()).ucumUnit().code());
    }
}
