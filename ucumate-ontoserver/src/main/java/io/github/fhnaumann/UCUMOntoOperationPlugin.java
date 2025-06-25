package io.github.fhnaumann;

import io.github.fhnaumann.operations.ValidateCodeOperation;

/**
 * @author Felix Naumann
 */
@OntoPlugin(name = "UCUMPlugin", systems = "http://unitsofmeasure.org")
public class UCUMOntoOperationPlugin implements OntoOperationPlugin {

    @Override
    public void initialize() {

    }

    @Override
    public ValidateCodeOperation validateCodeOperation() {
        return null;
    }
}
