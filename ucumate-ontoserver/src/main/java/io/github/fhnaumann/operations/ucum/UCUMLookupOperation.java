package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.CustomUnitMappingPrinter;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.operations.LookupOperation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of the $lookup operation for UCUM CodeSystems.
 * @author Felix Naumann
 */
public class UCUMLookupOperation implements LookupOperation {

    private static final Map<String, BiFunction<UCUMService, UCUMExpression.Term, String>> PROPS_MAPPER = Map.of(
            "code", (service1, term) -> service1.print(term, Printer.PrintType.UCUM_SYNTAX),
            "display", (service1, term) -> service1.print(term, Printer.PrintType.UCUM_SYNTAX), // todo agree on a display syntax?
            "name", (service1, term) -> "TODO: whats the display name for the UCUM code system?",
            "version", (service1, term) -> service1.getUCUMVersion().getVersion(),
            "latexSyntax", (service1, term) -> service1.print(term, Printer.PrintType.LATEX_SYNTAX),
            "codeCaseInsensitive", (service1, term) -> new CustomUnitMappingPrinter(UCUMDefinition.Concept::codeCaseInsensitive).print(term),
            "unitName", (service1, term) -> new CustomUnitMappingPrinter(concept -> concept.names().stream().findFirst().orElseThrow()).print(term)
    );

    private final Set<String> REQUESTABLE_PROPS = Set.of(
            "url", "name", "version", "display", "definition", "designation", "parent", "child", // FHIR defaults
            "code", "codeCaseInsensitive", "unitName", "property", "metric", "special", "class", "dimensionality", "canonical", "commonSyntax", "latexSyntax"
    );

    private final UCUMService service;

    public UCUMLookupOperation(UCUMService service) {
        this.service = service;
    }

    /**
     * {@inheritDoc}
     * <br>
     * Will try to parse the provided code using ucumate.
     * Supported properties are apart from the default ones are:
     * <lu>
     *     <li>latexSyntax: Creates a LaTeX string of the expression</li>
     *     <li>codeCaseInsensitive: Return in UCUM syntax but use the case-insensitive codes</li>
     *     <li>unitName: Return in UCUM syntax but use full unit names</li>
     * </lu>
     * @param coding The coding with a specified version.
     * @param properties Additional properties of the provided code to be returned.
     * @return
     */
    @Override
    public LookupResult lookup(Coding coding, Collection<String> properties) {
        /*
        code, display, and name are always returned.
        Additional property are returned as requested.
        An unknown property is silently ignored.
         */
        UcumVersion ucumVersion = matchUCUMVersion(coding.getVersion());
        if(ucumVersion == null) {
            OperationOutcome operationOutcome = constructOperationOutcome(coding);
            return new Failure(operationOutcome);
        }
        return switch (service.validate(coding.getCode())) {
            case ValidatorService.Failure failure -> new Failure(constructOperationOutcome(failure));
            case ValidatorService.Success success -> handleSuccess(properties, success);
        };
    }

    private LookupResult handleSuccess(Collection<String> properties, ValidatorService.Success success) {
        Map<String, String> returnedProps = properties.stream()
                .filter(REQUESTABLE_PROPS::contains)
                .collect(Collectors.toMap(
                        Function.identity(),
                        s -> PROPS_MAPPER.get(s).apply(service, success.term()) // todo what if get returns null because unknown prop?
                ));
        return new Success(
                PROPS_MAPPER.get("code").apply(service, success.term()),
                PROPS_MAPPER.get("name").apply(service, success.term()),
                PROPS_MAPPER.get("version").apply(service, success.term()),
                PROPS_MAPPER.get("display").apply(service, success.term()),
                returnedProps
        );
    }

    private OperationOutcome constructOperationOutcome(ValidatorService.Failure failure) {
        OperationOutcome operationOutcome = new OperationOutcome();
        List<OperationOutcome.OperationOutcomeIssueComponent> issues = failure.errorMessages().stream()
                .map(s -> new OperationOutcome.OperationOutcomeIssueComponent()
                        .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                        .setCode(OperationOutcome.IssueType.CODEINVALID)
                        .setDetails(new CodeableConcept().setText(s)))
                .toList();
        operationOutcome.setIssue(issues);
        return operationOutcome;
    }

    private OperationOutcome constructOperationOutcome(Coding coding) {
        String knownUCUMVersions = Arrays.stream(UcumVersion.values())
                .map(UcumVersion::getVersion)
                .collect(Collectors.joining(","));
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
                .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
                .setDetails(new CodeableConcept().setText("Version '%s' is not supported. Only '%s' are supported versions.".formatted(coding.getVersion(), knownUCUMVersions)));
        return operationOutcome;
    }

    private UcumVersion matchUCUMVersion(String versionInCoding) {
        try {
            return UcumVersion.fromVersionString(versionInCoding);
        } catch (Exception e) {
            return null;
        }
    }

}
