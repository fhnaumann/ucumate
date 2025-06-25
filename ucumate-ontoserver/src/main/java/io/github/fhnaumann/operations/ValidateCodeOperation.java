package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;

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

    public record ValidateCodeResult(boolean result, String display, String message) {}
}
