package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.ValueSet;

/**
 * @author Felix Naumann
 */
public interface ExpandCodeOperation {

    public ValueSet expand(ValueSet valueSet, String textFilter);
}
