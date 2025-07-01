package io.github.fhnaumann;

import io.github.fhnaumann.operations.ExpandOperation;
import io.github.fhnaumann.operations.LookupCodeOperation;
import io.github.fhnaumann.operations.ValidateCodeOperation;

/**
 * @author Felix Naumann
 */
public interface OntoOperationPlugin extends ExpandOperation, LookupCodeOperation, ValidateCodeOperation {
    void initialize();

}
