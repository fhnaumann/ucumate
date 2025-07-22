package io.github.fhnaumann.operations.ucum.issues;

import au.csiro.ontoserver.operations.validate.issues.IValidationIssue;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * @author Felix Naumann
 */
public class InvalidInputException extends Exception implements IValidationIssue {

    public InvalidInputException() {
    }

    public InvalidInputException(String message) {
        super(message);
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent getIssue() {
        return null;
    }

    @Override
    public boolean includeMessage() {
        return false;
    }
}
