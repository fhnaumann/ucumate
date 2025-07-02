package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.*;

import java.util.Map;

/**
 * @author Felix Naumann
 */
public interface ValidateCodeOperation {

    public ValidateCodeResult validate(ValueSet valueSet, CodeableConcept codeableConcept);

    public default ValidateCodeResult validate(ValueSet valueSet, Coding coding) {
        return validate(valueSet, new CodeableConcept(coding));
    }

    public default ValidateCodeResult validate(ValueSet valueSet, CodeType codeType) {
        return validate(valueSet, new Coding(codeType.getSystem(), codeType.getCode(), codeType.getDisplay()));
    }

    public sealed interface ValidateCodeResult {}

    public record Success(Map<Coding, Detail> details) implements ValidateCodeResult {
        public boolean result() {
            return details.values().stream().allMatch(Detail::valid);
        }
    }

    public record Detail(boolean valid, String message, String display) {}

    public record Failure(OperationOutcome operationOutcome) implements ValidateCodeResult {}

    public record ValidateCodeResult2(boolean result, String display, String message) {}
}
