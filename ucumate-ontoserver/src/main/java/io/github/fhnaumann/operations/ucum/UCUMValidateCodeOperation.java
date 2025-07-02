package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.UCUMOntoOperationPlugin;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ExpandOperation;
import io.github.fhnaumann.operations.ValidateCodeOperation;
import io.github.fhnaumann.util.LogUtil;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class UCUMValidateCodeOperation implements ValidateCodeOperation {

    private static final Logger log = LoggerFactory.getLogger(UCUMValidateCodeOperation.class);
    private final UCUMService ucumService;
    private final UCUMExpandOperation ucumExpandOperation;

    public UCUMValidateCodeOperation(UCUMService service, UCUMExpandOperation ucumExpandOperation) {
        this.ucumService = service;
        this.ucumExpandOperation = ucumExpandOperation;
    }

    @Override
    public ValidateCodeResult validate(CodeSystem codeSystem, CodeableConcept codeableConcept) {
        if(codeSystem.getUrl().equals(UCUMOntoOperationPlugin.UCUM_SYSTEM) || codeSystem.getContent() == CodeSystem.CodeSystemContentMode.NOTPRESENT) {
            // the canonical UCUM CodeSystem - include *all* UCUM terms (an empty ValueSet)
            return validate(new ValueSet(), codeableConcept);
        }
        // a definitive subset of UCUM codes are provided, only validate against them by copying them into a ValueSet and calling the validate method
        ValueSet valueSet = new ValueSet();
        codeSystem.getConcept().forEach(concept -> {
            ValueSet.ConceptSetComponent setComponent = new ValueSet.ConceptSetComponent();
                    setComponent.addConcept().setCode(concept.getCode()).setDisplay(concept.getDisplay());
            valueSet.getCompose().getInclude().add(setComponent);
        });
        validate(valueSet, codeableConcept);
        return null;
    }

    @Override
    public ValidateCodeResult validate(ValueSet valueSet, CodeableConcept codeableConcept) {
        Map<Coding, ValidatorService.ValidationResult> results;
        if(ucumExpandOperation.includesAllUCUMCodes(valueSet)) {
            results = performValidation(codeableConcept);
        }
        else {
            results = performLimitedValidation(valueSet, codeableConcept);
        }
        Map<Coding, Detail> details = createDetails(results);
        return new Success(details);

    }

    private Map<Coding, Detail> createDetails(Map<Coding, ValidatorService.ValidationResult> results) {
        Map<Coding, Detail> details = new LinkedHashMap<>();
        results.forEach((coding, result) -> {
            Detail detail = null;
            if(coding.getDisplay() != null && !coding.getCode().equals(coding.getDisplay())) {
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

    private LinkedHashMap<Coding, ValidatorService.ValidationResult> performLimitedValidation(ValueSet valueSet, CodeableConcept codeableConcept) {
        ExpandOperation.ExpandResult expandResult = ucumExpandOperation.expand(valueSet, null);
        return switch (expandResult) {
            case ExpandOperation.Failure failure -> LogUtil.logAndThrow(log, "Unexpected expansion failure.");
            case ExpandOperation.Success success -> checkIfInExpandedValueSet(codeableConcept, valueSet);
        };
    }

    private LinkedHashMap<Coding, ValidatorService.ValidationResult> checkIfInExpandedValueSet(CodeableConcept codeableConcept, ValueSet expandedVs) {
        return codeableConcept.getCoding().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        coding -> fromLimited(coding, expandedVs),
                        (result, result2) -> result,
                        LinkedHashMap::new
                ));
    }

    private ValidatorService.ValidationResult fromLimited(Coding coding, ValueSet expandedVs) {
        if(hasCode(coding.getCode(), expandedVs)) {
            return switch (ucumService.validate(coding.getCode())) {
                case ValidatorService.Failure failure -> LogUtil.logAndThrow(log, "Unexpected validation failure after expansion included it for code '{}': {}", coding.getCode(), failure.errorMessages());
                case ValidatorService.Success success -> success;
            };
        }
        // This is a slight misuse of the ValidatorService return types because the scope here is wider than the return types should allow/cover
        return new ValidatorService.Failure("'%s' is valid itself but it is not in the expanded ValueSet.".formatted(coding.getCode()));
    }

    private boolean hasCode(String code, ValueSet expandedVs) {
        return expandedVs.getExpansion().getContains().stream()
                .anyMatch(comp -> comp.getCode().equals(code));
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
