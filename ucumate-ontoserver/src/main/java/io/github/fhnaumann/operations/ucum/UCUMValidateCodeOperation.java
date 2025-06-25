package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.operations.ValidateCodeOperation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class UCUMValidateCodeOperation implements ValidateCodeOperation {

    private final UCUMService ucumService = new UCUMService();

    @Override
    public ValidateCodeResult validate(ValueSet valueSet, CodeableConcept codeableConcept) {
        Map<String, ValidatorService.ValidationResult> results = performValidation(codeableConcept);
        boolean valid = isValid(results);
        if(valid) {
            String display = createDisplayMessage(results);
            return new ValidateCodeResult(true, display, null);
        }
        else {
            String errorMessage = createErrorMessage(results);
            return new ValidateCodeResult(false, null, errorMessage);
        }
    }

    private boolean isValid(Map<String, ValidatorService.ValidationResult> results) {
        return results.values().stream().allMatch(ValidatorService.Success.class::isInstance);
    }

    private String createErrorMessage(Map<String, ValidatorService.ValidationResult> results) {
        return results.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ValidatorService.Failure)
                .map(entry -> Map.entry(entry.getKey(), String.join(",", ((ValidatorService.Failure) entry.getValue()).errorMessages())))
                .map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(","));
    }

    private String createDisplayMessage(Map<String, ValidatorService.ValidationResult> results) {
        return results.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof ValidatorService.Success)
                .map(entry -> Map.entry(entry.getKey(), ((ValidatorService.Success) entry.getValue()).term()))
                .map(entry -> "%s: %s".formatted(entry.getKey(), ucumService.print(entry.getValue(), Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX)))
                .collect(Collectors.joining(","));
    }

    private LinkedHashMap<String, ValidatorService.ValidationResult> performValidation(CodeableConcept codeableConcept) {
        return codeableConcept.getCoding().stream()
                .collect(Collectors.toMap(
                        Coding::getCode,
                        coding -> ucumService.validate(coding.getCode()),
                        (o, o2) -> o,
                        LinkedHashMap::new
                ));
    }
}
