package io.github.fhnaumann.operations.ucum.issues;

import au.csiro.ontoserver.operations.validate.issues.IValidationIssue;
import io.github.fhnaumann.model.UCUMExpression;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * @author Felix Naumann
 */
public class AnnotationUsageWarning implements IValidationIssue {

    private final String termWithAnnotation;
    private final OperationOutcome.OperationOutcomeIssueComponent issueComponent;

    public AnnotationUsageWarning(String termWithAnnotation) {
        this.termWithAnnotation = termWithAnnotation;
        this.issueComponent = new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(OperationOutcome.IssueSeverity.WARNING)
                .setCode(OperationOutcome.IssueType.INFORMATIONAL)
                .setDetails(new CodeableConcept().setText(getMessage()));
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent getIssue() {
        return issueComponent;
    }

    @Override
    public String getMessage() {
        return "'%s' has an annotation. The usage of annotations in UCUM is discouraged.".formatted(termWithAnnotation);
    }

    @Override
    public boolean includeMessage() {
        return true;
    }
}
