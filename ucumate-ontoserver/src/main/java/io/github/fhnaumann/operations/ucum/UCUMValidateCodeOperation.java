package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ValidateCodeOperation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class UCUMValidateCodeOperation implements ValidateCodeOperation {

    private final UCUMService ucumService = new UCUMService();

    @Override
    public ValidateCodeResult validate(ValueSet valueSet, CodeableConcept codeableConcept) {
        Map<Coding, ValidatorService.ValidationResult> results = performValidation(codeableConcept);
        Map<Coding, Detail> details = createDetails(results);
        return new Success(details);
    }

    private Map<Coding, Detail> createDetails(Map<Coding, ValidatorService.ValidationResult> results) {
        Map<Coding, Detail> details = new LinkedHashMap<>();
        results.forEach((coding, result) -> {
            Detail detail = null;
            if(!displayNullOrEqualToCode(coding)) {
                String message = "The display '%s' does not match the provided code '%s'. In UCUM code and display are the same.".formatted(coding.getDisplay(), coding.getCode());
                detail = new Detail(false, message, null);
            }
            detail = switch (result) {
                case ValidatorService.Success success -> new Detail(
                        detail == null,
                        detail == null ? addPotentialWarnings(coding.getCode(), success.term()) : detail.message(),
                        detail == null ? coding.getCode() : null
                );
                case ValidatorService.Failure failure -> new Detail(
                        false,
                        "The code '%s' is invalid: %s".formatted(coding.getCode(), String.join(",", failure.errorMessages())), null);
            };
            details.put(coding, detail);
        });
        return details;
    }

    private String addPotentialWarnings(String code, UCUMExpression.Term term) {
        if(containsAnnotationImpl(term)) {
            return "%s: The usage of annotations in UCUM expressions is discouraged.".formatted(code);
        }
        return null;
    }

    private boolean isValid(Map<Coding, ValidatorService.ValidationResult> results) {
        return results.values().stream().allMatch(this::isSuccess)
                && results.keySet().stream().allMatch(this::displayNullOrEqualToCode);
    }

    private boolean isSuccess(ValidatorService.ValidationResult result) {
        return result instanceof ValidatorService.Success;
    }

    private boolean displayNullOrEqualToCode(Coding coding) {
        return coding == null || coding.getDisplay().equals(coding.getCode());
    }

    private boolean containsAnnotationImpl(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> false;
            case UCUMExpression.AnnotTerm annotTerm -> true;
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> true;
            case UCUMExpression.ParenTerm parenTerm -> containsAnnotationImpl(parenTerm.term());
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> containsAnnotationImpl(unaryDivTerm.term());
            case UCUMExpression.BinaryTerm binaryTerm ->
                    containsAnnotationImpl(binaryTerm.left()) || containsAnnotationImpl(binaryTerm.right());
        };
    }

    private LinkedHashMap<Coding, ValidatorService.ValidationResult> performValidation(CodeableConcept codeableConcept) {
        return codeableConcept.getCoding().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        coding -> ucumService.validate(coding.getCode()),
                        (o, o2) -> o,
                        LinkedHashMap::new
                ));
    }
}
