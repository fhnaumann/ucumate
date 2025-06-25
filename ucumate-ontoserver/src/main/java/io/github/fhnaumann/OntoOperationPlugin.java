package io.github.fhnaumann;

import io.github.fhnaumann.operations.ValidateCodeOperation;

/**
 * @author Felix Naumann
 */
public interface OntoOperationPlugin {

    void initialize();

    ValidateCodeOperation validateCodeOperation();
}
