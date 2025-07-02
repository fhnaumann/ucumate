package io.github.fhnaumann;

import io.github.fhnaumann.operations.ExpandOperation;
import io.github.fhnaumann.operations.LookupOperation;
import io.github.fhnaumann.operations.ValidateCodeOperation;

/**
 * @author Felix Naumann
 */
public interface OntoOperationPlugin extends ExpandOperation, LookupOperation, ValidateCodeOperation {
    void initialize();
}
