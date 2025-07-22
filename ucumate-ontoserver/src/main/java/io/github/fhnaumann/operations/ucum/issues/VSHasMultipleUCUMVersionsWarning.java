package io.github.fhnaumann.operations.ucum.issues;

import au.csiro.ontoserver.operations.validate.issues.IValidationIssue;
import io.github.fhnaumann.model.UcumVersion;
import org.hl7.fhir.r4.model.OperationOutcome;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class VSHasMultipleUCUMVersionsWarning implements IValidationIssue {

    private final UcumVersion codingVersion;
    private final List<UcumVersion> otherVersionsFound;
    private final OperationOutcome.OperationOutcomeIssueComponent issueComponent;

    public VSHasMultipleUCUMVersionsWarning(UcumVersion codingVersion, List<UcumVersion> otherVersionsFound) {
        this.codingVersion = codingVersion;
        this.otherVersionsFound = otherVersionsFound;
        this.issueComponent = new OperationOutcome.OperationOutcomeIssueComponent()
                .setCode(OperationOutcome.IssueType.INFORMATIONAL)
                .setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent getIssue() {
        return issueComponent;
    }

    @Override
    public String getMessage() {
        return "Used UCUM version '%s' for the provided code but found different UCUM versions in ValueSet: '%s'"
                .formatted(codingVersion.getVersion(), otherVersionsFound.stream().map(UcumVersion::getVersion).collect(Collectors.joining(",")));
    }

    @Override
    public boolean includeMessage() {
        return true;
    }
}
