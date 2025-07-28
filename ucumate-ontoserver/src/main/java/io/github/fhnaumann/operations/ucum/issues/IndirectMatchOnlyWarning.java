package io.github.fhnaumann.operations.ucum.issues;

import au.csiro.ontoserver.operations.validate.issues.IValidationIssue;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;

/**
 * @author Felix Naumann
 */
public class IndirectMatchOnlyWarning implements IValidationIssue {

    private final String source;
    private final String semanticMatch;
    private final OperationOutcome.OperationOutcomeIssueComponent issueComponent;

    public IndirectMatchOnlyWarning(String source, String semanticMatch) {
        issueComponent = new OperationOutcome.OperationOutcomeIssueComponent()
                .setCode(OperationOutcome.IssueType.INFORMATIONAL)
                .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                .setDetails(new CodeableConcept()
                        .setText(getMessage()));
        this.source = source;
        this.semanticMatch = semanticMatch;
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent getIssue() {
        return issueComponent;
    }

    @Override
    public String getMessage() {
        return "'%s' is only semantically equivalent to '%s', not syntactically.".formatted(source, semanticMatch);
    }

    @Override
    public boolean includeMessage() {
        return true;
    }
}
