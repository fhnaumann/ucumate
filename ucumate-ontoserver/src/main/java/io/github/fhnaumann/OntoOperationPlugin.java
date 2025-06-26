package io.github.fhnaumann;

import io.github.fhnaumann.operations.ExpandCodeOperation;
import io.github.fhnaumann.operations.LookupCodeOperation;
import io.github.fhnaumann.operations.ValidateCodeOperation;

/**
 * @author Felix Naumann
 */
public interface OntoOperationPlugin extends ExpandCodeOperation, LookupCodeOperation, ValidateCodeOperation {
    void initialize();

}
