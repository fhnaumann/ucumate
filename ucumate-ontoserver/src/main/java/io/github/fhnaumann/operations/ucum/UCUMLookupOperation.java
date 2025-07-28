package io.github.fhnaumann.operations.ucum;

import au.csiro.ontoserver.exceptions.*;
import au.csiro.ontoserver.operations.lookup.LookupOperation;
import au.csiro.ontoserver.operations.lookup.LookupProcessor;
import au.csiro.ontoserver.operations.lookup.LookupProfile;
import au.csiro.ontoserver.operations.validate.ValidateCodeOperation;
import io.github.fhnaumann.UCUMOntoOperationPlugin;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.CustomUnitMappingPrinter;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import org.hl7.fhir.r4.model.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of the $lookup operation for UCUM CodeSystems.
 * @author Felix Naumann
 */
public class UCUMLookupOperation implements LookupOperation {

    private static final Map<String, BiFunction<UCUMService, UCUMExpression.Term, String>> PROPS_MAPPER = Map.of(
            "code", (service1, term) -> service1.print(term, Printer.PrintType.UCUM_SYNTAX),
            "display", (service1, term) -> service1.print(term, Printer.PrintType.UCUM_SYNTAX), // todo agree on a display syntax?
            "name", (service1, term) -> "UCUM",
            "version", (service1, term) -> service1.getUCUMVersion().getVersion(),
            "latexSyntax", (service1, term) -> service1.print(term, Printer.PrintType.LATEX_SYNTAX),
            "codeCaseInsensitive", (service1, term) -> new CustomUnitMappingPrinter(UCUMDefinition.Concept::codeCaseInsensitive).print(term),
            "unitName", (service1, term) -> new CustomUnitMappingPrinter(concept -> concept.names().stream().findFirst().orElseThrow()).print(term)
    );

    private static final List<String> MANDATORY_RETURN_PROPS = List.of("code", "display", "name");

    private final Set<String> REQUESTABLE_PROPS = Set.of(
            "url", "name", "version", "display", "definition", "designation", "parent", "child", // FHIR defaults
            "code", "codeCaseInsensitive", "unitName", "property", "metric", "special", "class", "dimensionality", "canonical", "commonSyntax", "latexSyntax"
    );

    private final UCUMOntoOperationPlugin plugin;
    private final UCUMService service;

    public UCUMLookupOperation(UCUMOntoOperationPlugin plugin, UCUMService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @Override
    public void lookup(Coding coding, LookupProfile lookupProfile, LookupProcessor lookupProcessor) throws PluginBaseException {
        if(!UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(coding.getSystem())) {
            return;
        }
        UcumVersion ucumVersion = UcumVersion.V2_2;
        if(coding.hasVersion()) {
            ucumVersion = UcumVersion.fromVersionStringAsOpt(coding.getVersion())
                    .orElseThrow(() -> new PluginUnprocessableEntityException("Unknown UCUM version '%s' for code '%s'.".formatted(coding.getVersion(), coding.getCode()), plugin));
        }
        service.setUCUMVersion(ucumVersion);

        Parameters parameters = new Parameters();
        List<String> requestedProps = new ArrayList<>();
        if(lookupProfile.allProperties()) {
            requestedProps.addAll(PROPS_MAPPER.keySet());
        }
        else {
            checkForUnknownProps(lookupProfile.properties());
            requestedProps.addAll(lookupProfile.properties());
        }
        switch (service.validate(coding.getCode())) {
            case ValidatorService.Failure failure -> throw new PluginCodeNotFoundException(String.join(",", failure.errorMessages()), plugin);
            case ValidatorService.Success success -> handleCodeParseSuccess(success.term(), requestedProps, parameters);
        }
        lookupProcessor.result(parameters);
    }

    private void handleCodeParseSuccess(UCUMExpression.Term term, Collection<String> requestedProps, Parameters parameters) {
        MANDATORY_RETURN_PROPS.stream()
                .filter(Predicate.not(requestedProps::contains))
                .forEach(requestedProps::add);
        requestedProps.stream()
                .map(prop -> Map.entry(prop, PROPS_MAPPER.get(prop).apply(service, term)))
                .forEach(entry -> parameters.addParameter(entry.getKey(), entry.getValue()));
        Parameters.ParametersParameterComponent nameDesignation = parameters.addParameter();
        nameDesignation.setName("designation");
        nameDesignation.addPart()
                .setName("language")
                .setValue(new StringType("en"));
        nameDesignation.addPart()
                .setName("use")
                .setValue(new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/designation-usage")
                    .setCode("display")
                    .setDisplay("Display"));
        nameDesignation.addPart()
                .setName("value")
                .setValue(new StringType(service.print(term, Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX)));
                //.setValue(new StringType(new CustomUnitMappingPrinter(concept -> concept.names().stream().findFirst().orElseThrow()).print(term)));

        // todo add warning parameter for annotation usage?
    }

    private void checkForUnknownProps(Collection<String> requestedProps) {
        requestedProps.removeIf(Predicate.not(PROPS_MAPPER::containsKey));
    }


//    /**
//     * {@inheritDoc}
//     * <br>
//     * Will try to parse the provided code using ucumate.
//     * Supported properties are apart from the default ones are:
//     * <lu>
//     *     <li>latexSyntax: Creates a LaTeX string of the expression</li>
//     *     <li>codeCaseInsensitive: Return in UCUM syntax but use the case-insensitive codes</li>
//     *     <li>unitName: Return in UCUM syntax but use full unit names</li>
//     * </lu>
//     * @param coding The coding with a specified version.
//     * @param properties Additional properties of the provided code to be returned.
//     * @return
//     */
//    @Override
//    public LookupOperation.LookupResult lookup(Coding coding, Collection<String> properties) {
//        /*
//        code, display, and name are always returned.
//        Additional property are returned as requested.
//        An unknown property is silently ignored.
//         */
//        UcumVersion ucumVersion = matchUCUMVersion(coding.getVersion());
//        if(ucumVersion == null) {
//            OperationOutcome operationOutcome = constructOperationOutcome(coding);
//            return new Failure(operationOutcome);
//        }
//        return switch (service.validate(coding.getCode())) {
//            case ValidatorService.Failure failure -> new Failure(constructOperationOutcome(failure));
//            case ValidatorService.Success success -> handleSuccess(properties, success);
//        };
//    }
//
//    private LookupResult handleSuccess(Collection<String> properties, ValidatorService.Success success) {
//        Map<String, String> returnedProps = properties.stream()
//                .filter(REQUESTABLE_PROPS::contains)
//                .collect(Collectors.toMap(
//                        Function.identity(),
//                        s -> PROPS_MAPPER.get(s).apply(service, success.term()) // todo what if get returns null because unknown prop?
//                ));
//        return new Success(
//                PROPS_MAPPER.get("code").apply(service, success.term()),
//                PROPS_MAPPER.get("name").apply(service, success.term()),
//                PROPS_MAPPER.get("version").apply(service, success.term()),
//                PROPS_MAPPER.get("display").apply(service, success.term()),
//                returnedProps
//        );
//    }
//
//    private OperationOutcome constructOperationOutcome(ValidatorService.Failure failure) {
//        OperationOutcome operationOutcome = new OperationOutcome();
//        List<OperationOutcome.OperationOutcomeIssueComponent> issues = failure.errorMessages().stream()
//                .map(s -> new OperationOutcome.OperationOutcomeIssueComponent()
//                        .setSeverity(OperationOutcome.IssueSeverity.ERROR)
//                        .setCode(OperationOutcome.IssueType.CODEINVALID)
//                        .setDetails(new CodeableConcept().setText(s)))
//                .toList();
//        operationOutcome.setIssue(issues);
//        return operationOutcome;
//    }
//
//    private OperationOutcome constructOperationOutcome(Coding coding) {
//        String knownUCUMVersions = Arrays.stream(UcumVersion.values())
//                .map(UcumVersion::getVersion)
//                .collect(Collectors.joining(","));
//        OperationOutcome operationOutcome = new OperationOutcome();
//        operationOutcome.addIssue()
//                .setSeverity(OperationOutcome.IssueSeverity.ERROR)
//                .setCode(OperationOutcome.IssueType.NOTSUPPORTED)
//                .setDetails(new CodeableConcept().setText("Version '%s' is not supported. Only '%s' are supported versions.".formatted(coding.getVersion(), knownUCUMVersions)));
//        return operationOutcome;
//    }
//
//    private UcumVersion matchUCUMVersion(String versionInCoding) {
//        try {
//            return UcumVersion.fromVersionString(versionInCoding);
//        } catch (Exception e) {
//            return null;
//        }
//    }
}
