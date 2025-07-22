package io.github.fhnaumann.operations.ucum;

import au.csiro.ontoserver.exceptions.*;
import au.csiro.ontoserver.operations.expand.ExpansionProfile;
import au.csiro.ontoserver.operations.validate.ICodingValidationResult;
import au.csiro.ontoserver.operations.validate.ValidateCodeOperation;
import au.csiro.ontoserver.operations.validate.ValidateCodeProfile;
import au.csiro.ontoserver.operations.validate.ValidateProcessor;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import io.github.fhnaumann.PluginUtil;
import io.github.fhnaumann.UCUMOntoOperationPlugin;
import io.github.fhnaumann.funcs.RelationChecker;
import io.github.fhnaumann.funcs.RelationCheckerService;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.operations.ucum.issues.AnnotationUsageWarning;
import io.github.fhnaumann.operations.ucum.issues.IndirectMatchOnlyWarning;
import io.github.fhnaumann.operations.ucum.issues.VSHasMultipleUCUMVersionsWarning;
import io.github.fhnaumann.util.LogUtil;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.fhnaumann.UCUMOntoOperationPlugin.IS_UCUM_SYSTEM_;

/**
 * @author Felix Naumann
 */
public class UCUMValidateCodeOperation implements ValidateCodeOperation {

    private static final Logger log = LoggerFactory.getLogger(UCUMValidateCodeOperation.class);

    private static final boolean ALLOW_MOL_MASS_CONVERSION = false; // todo figure out how to integrate that with ontoserver (or pull from config)

    private final UCUMOntoOperationPlugin plugin;
    private final UCUMService ucumService;
    private final UCUMExpandOperation ucumExpandOperation;

    public UCUMValidateCodeOperation(UCUMOntoOperationPlugin plugin, UCUMService service, UCUMExpandOperation ucumExpandOperation) {
        this.plugin = plugin;
        this.ucumService = service;
        this.ucumExpandOperation = ucumExpandOperation;
    }

    /**
     *
     * Multi-version support is quite complex, below is the intended behavior.
     * <br>
     * If the coding has a (valid) version, then this is used as the basis for the validation. If no version is specified,
     * then the latest UCUM version will be used.
     * The code system versions in the ValueSet can exist at various places. I.e. a compose block can have a version.
     * It's trivial when these versions match the coding version, but it's more complex when they don't.
     * UCUM code systems have concept permanence and therefore using multiple versions in the same request is salvageable (to some degree).
     * Here are some examples:
     * If the Coding.version is 2.2 but a filter with 2.1 is included, then the validation will progress. If the Coding.code
     * is a code that exists in 2.2 but not in 2.1, then the result is false (but no error, maybe a potential warning). It's the same behavior for
     * Coding.display (unlike codes, display sometimes changes throughout versions).
     * If the Coding.version is 2.2. and there are two filters with 2.1 and 2.2, then it will try to use the matching version.
     *
     * @param valueSet
     * @param coding
     * @param expansionProfile
     * @param validateCodeProfile
     * @param validateProcessor
     * @throws PluginBaseException
     */
    @Override
    public void validateCode(ValueSet valueSet, Coding coding, ExpansionProfile expansionProfile, ValidateCodeProfile validateCodeProfile, ValidateProcessor validateProcessor) throws PluginBaseException {
        if(!UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(coding.getSystem())) {
            return;
        }
        try {
            switch (ucumService.validate(coding.getCode())) {
                case ValidatorService.Failure failure -> handleFailure(failure);
                case ValidatorService.Success success -> handleSuccess(valueSet, coding, expansionProfile, validateProcessor, success);
            }
        } catch (WrappingCheckedException e) {
            throw e.getUnderlyingException();
        }
    }

    private void handleFailure(ValidatorService.Failure failure) throws PluginCodeNotFoundException {
        throw new PluginCodeNotFoundException(String.join(",", failure.errorMessages()), plugin);
    }

    private void handleSuccess(ValueSet valueSet, Coding coding, ExpansionProfile expansionProfile, ValidateProcessor validateProcessor, ValidatorService.Success success) throws PluginUnprocessableEntityException {
        // todo check if expansion exists, if so, only check expansion, otherwise do stuff with compose below
        /*
         The code is valid, but it may not be included based on the VS composes.
         The easiest way to check would be to expand the VS and check if the code is in the expansion, but that can be expensive.
         It's more performant to check the compose block (especially the filter) in a "smart" way.

         has "include" concept code?
         yes: check if parsed term is "semantically" equal to a code in the include concept block (add a warning if only semantically equal and not syntactically equal)
         no: include filter?
             no: valid, because the all UCUM codes are used as basis, and it already passed parsing
             yes: smartly check against filter and determine validity

         has "exclude" concept code?
         yes: check if parsed term is "semantically" equal to a code in the exclude concept block ((add a warning if only semantically equal and not syntactically equal)

         */

        UcumVersion ucumVersion = UcumVersion.getLatest();
        if(coding.hasVersion()) {
            ucumVersion = UcumVersion.fromVersionStringAsOpt(coding.getVersion()).orElseThrow(() -> new PluginUnprocessableEntityException("Unknown UCUM version '%s'".formatted(coding.getVersion()), plugin));
        }
        ucumService.setUCUMVersion(ucumVersion);
        validateProcessor.getCodingValidationResult().setVersion(ucumVersion.getVersion());

        // check for annotation and add warning
        if(hasAnnotation(success.term())) {
            validateProcessor.getCodingValidationResult().addIssue(new AnnotationUsageWarning(ucumService.print(success.term(), Printer.PrintType.UCUM_SYNTAX)));
        }

        List<ValueSet.ConceptReferenceComponent> excludeConceptRefs = getConceptRefs(valueSet.getCompose().getExclude());
        if(excludeConceptRefs.stream().anyMatch(ValueSet.ConceptReferenceComponent::hasCode)) {
            // there is an exclude concept block with codes (for UCUM) in the VS
            // if the code that is to be validated, matches here, it's automatically invalid because it's explicitly (either directly as a code, or through a filter) excluded from the VS
            List<UCUMExpression.Term> parsedExcludeConceptRefs = parseConceptRefsOrThrowIfFailed(excludeConceptRefs, validateProcessor);
            RelationCheckerService.IsEqual equalMatch = checkEquality(coding, success.term(), parsedExcludeConceptRefs);
            if(equalMatch != null) {
                if(equalMatch.strictEqual()) {
                    validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.INVALID);
                }
                else if(equalMatch.equalAfterProcessing()) {
                    validateProcessor.getCodingValidationResult().addIssue(new IndirectMatchOnlyWarning(coding.getCode(), ucumService.print(equalMatch.termThatIsEqual(), Printer.PrintType.UCUM_SYNTAX)));
                    validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.INVALID);
                }
                return;
            }
        }
        List<ValueSet.ConceptSetFilterComponent> excludeFilters = valueSet.getCompose().getExclude().stream()
                .filter(conceptSetComponent -> UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(conceptSetComponent.getSystem()))
                .flatMap(conceptSetComponent -> conceptSetComponent.getFilter().stream())
                .toList();
        excludeFilters.forEach(conceptSetFilterComponent -> {
            smartlyAnalyzeFilter(success.term(), conceptSetFilterComponent, false, validateProcessor);
        });

        if(validateProcessor.getCodingValidationResult().getStatus() != ICodingValidationResult.Status.UNKNOWN) {
            // the result could be determined just from the "exclude" block (i.e. the coding is "in" the "excluding" block and therefore always invalid
            return;
        }
        // there was no match against any code in the exclude concept refs, continue checking the includes now
        List<ValueSet.ConceptReferenceComponent> includeConceptRefs = getConceptRefs(valueSet.getCompose().getInclude());
        if(includeConceptRefs.stream().anyMatch(ValueSet.ConceptReferenceComponent::hasCode)) {
            // there is an include concept block with codes (for UCUM) in the VS
            List<UCUMExpression.Term> parsedConceptRefs = parseConceptRefsOrThrowIfFailed(includeConceptRefs, validateProcessor);
            RelationCheckerService.IsEqual equalMatch = checkEquality(coding, success.term(), parsedConceptRefs);
            if(equalMatch != null) {
                if(equalMatch.strictEqual()) {
                    // direct syntactic match
                    validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.VALID);
                }
                else if(equalMatch.equalAfterProcessing()) {
                    // indirect semantic match
                    validateProcessor.getCodingValidationResult().addIssue(new IndirectMatchOnlyWarning(coding.getCode(), ucumService.print(equalMatch.termThatIsEqual(), Printer.PrintType.UCUM_SYNTAX)));
                    validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.VALID);
                }
                return;
            }
            // there was no match against any code in the concept refs
            validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.INVALID);
            return;
        }
        // there are no direct codes in the include block, just filters
        List<ValueSet.ConceptSetFilterComponent> filters = valueSet.getCompose().getInclude().stream()
                .filter(conceptSetComponent -> UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(conceptSetComponent.getSystem()))
                .flatMap(conceptSetComponent -> conceptSetComponent.getFilter().stream())
                .toList();
        if(filters.isEmpty()
                && valueSet.getCompose().hasInclude()
                && !valueSet.getCompose().getInclude().isEmpty()
                && UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(valueSet.getCompose().getIncludeFirstRep().getSystem())) {
            // the entire UCUM system is included and since parsing was a success, the validation passes
            validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.VALID);
        }
        else {
            filters.forEach(conceptSetFilterComponent -> {
                smartlyAnalyzeFilter(success.term(), conceptSetFilterComponent, true, validateProcessor);
            });
        }


        // if no error was thrown previously, append the potential version warning
        // todo how do to handle different versions in the same valueset? Its complicated...
        //List<UcumVersion> versionsOtherThanCodingVersionPresentInVS = determineVersionsOtherThanCodingVersionPresentInVS(valueSet, ucumVersion);
        //if(!versionsOtherThanCodingVersionPresentInVS.isEmpty()) {
        //    validateProcessor.getCodingValidationResult().addIssue(new VSHasMultipleUCUMVersionsWarning(ucumVersion, versionsOtherThanCodingVersionPresentInVS));
            // try with the same validation with the other versions
        //}
    }

    private boolean hasAnnotation(UCUMExpression.Term term) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> false;
            case UCUMExpression.ParenTerm parenTerm -> hasAnnotation(parenTerm.term());
            case UCUMExpression.AnnotTerm annotTerm -> true;
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> true;
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> hasAnnotation(unaryDivTerm.term());
            case UCUMExpression.BinaryTerm binaryTerm -> hasAnnotation(binaryTerm.left()) || hasAnnotation(binaryTerm.right());
        };
    }

    private List<UcumVersion> determineVersionsOtherThanCodingVersionPresentInVS(ValueSet valueSet, UcumVersion codingUCUMVersion) {
        List<UcumVersion> versionDiff = new ArrayList<>();
        if(valueSet.hasCompose()) {
            List<UcumVersion> versionDiffInCompose = Stream.concat(valueSet.getCompose().getInclude().stream(), valueSet.getCompose().getExclude().stream())
                    .filter(UCUMOntoOperationPlugin.IS_UCUM_SYSTEM)
                    .map(conceptSetComponent -> UcumVersion.fromVersionStringAsOpt(conceptSetComponent.getVersion()).orElse(codingUCUMVersion))
                    .filter(ucumVersion -> codingUCUMVersion != ucumVersion)
                    .toList();
            versionDiff.addAll(versionDiffInCompose);
        }
        if(valueSet.hasExpansion()) {
            List<UcumVersion> versionDiffInExpansion = valueSet.getExpansion().getContains().stream()
                    .filter(UCUMOntoOperationPlugin.IS_UCUM_SYSTEM_)
                    .map(valueSetExpansionContainsComponent -> UcumVersion.fromVersionStringAsOpt(valueSetExpansionContainsComponent.getVersion()).orElse(codingUCUMVersion))
                    .filter(ucumVersion -> codingUCUMVersion != ucumVersion)
                    .toList();
            versionDiff.addAll(versionDiffInExpansion);
        }
        return versionDiff;
    }

    private void smartlyAnalyzeFilter(UCUMExpression.Term parsedTerm, ValueSet.ConceptSetFilterComponent conceptSetFilterComponent, boolean includeBlock, ValidateProcessor validateProcessor) {
        /*
        property filter:
            - a base unit property is provided, i.e. "length", "mass", "luminous intensity" (they correspond to a base unit).
            - get the base unit and check if commensurable to the parsed term
        canonical filter:
            - a term is provided, i.e. "m", "[ft_i]", "[ft_i]/s"
            - compare parsed term to term in filter
         */
        if(!UCUMOntoOperationPlugin.KNOWN_FILTERS.contains(conceptSetFilterComponent.getProperty())) {
            throw new Unchecked.UncheckedUnprocessableEntityException("Unknown filter '%s'.".formatted(conceptSetFilterComponent.getProperty()), plugin);
        }
        if(conceptSetFilterComponent.getProperty().equals("property") && conceptSetFilterComponent.getOp() == ValueSet.FilterOperator.EQUAL) {
            UCUMExpression.Term baseUnit = PluginUtil.getBaseUnitFromBaseProperty(conceptSetFilterComponent.getValue(), plugin, ucumService.getUCUMVersion());
            checkCommensurability(baseUnit, parsedTerm, includeBlock, validateProcessor);
        }
        else if(conceptSetFilterComponent.getProperty().equals("canonical")) {
            switch (conceptSetFilterComponent.getOp()) {
                case EQUAL -> handleCanonicalFilterEqualOp(parsedTerm, conceptSetFilterComponent.getValue(), includeBlock, validateProcessor);
                case IN -> handleCanonicalFilterInOp(parsedTerm, conceptSetFilterComponent.getValue());
                default -> throw new Unchecked.UncheckedUnprocessableEntityException("Operator '%s' for '%s' is not supported.".formatted(conceptSetFilterComponent.getOp(), conceptSetFilterComponent.getProperty()), plugin);
            }
        }
    }

    private void checkCommensurability(UCUMExpression.Term term1, UCUMExpression.Term term2, boolean includeBlock, ValidateProcessor validateProcessor) {
        switch (ucumService.checkCommensurable(term1, term2)) {
            case RelationCheckerService.FailedCommensurableCheck failedCommensurableCheck -> throw new Unchecked.UncheckedUnprocessableEntityException("Commensurability check failed.", plugin);
            case RelationCheckerService.NotCommensurable notCommensurable -> handleNotCommensurable(notCommensurable, includeBlock, validateProcessor);
            case RelationCheckerService.IsCommensurable isCommensurable -> handleIsCommensurable(isCommensurable, includeBlock, validateProcessor);
        }
    }

    private void handleCanonicalFilterEqualOp(UCUMExpression.Term parsedTerm, String value, boolean includeBlock, ValidateProcessor validateProcessor) {
        switch (ucumService.validate(value)) {
            case ValidatorService.Failure failure -> throw new Unchecked.UncheckedUnprocessableEntityException(String.join(",", failure.errorMessages()), plugin);
            case ValidatorService.Success success -> checkCommensurability(success.term(), parsedTerm, includeBlock, validateProcessor);
        }
    }

    private void handleCanonicalFilterInOp(UCUMExpression.Term parsedTerm, String value) {
        throw new RuntimeException("Implement later.");
    }

    private void handleNotCommensurable(RelationCheckerService.NotCommensurable notCommensurable, boolean includeBlock, ValidateProcessor validateProcessor) {
        if(!includeBlock) {
            /*
            The units are not commensurable in the EXCLUDE block, which means we can check the include block now and determine a status result
             */
            return;
        }
        // If in the exclude block, and they are not commensurable, then the code is excluded from the VS which makes it invalid in this check
        validateProcessor.getCodingValidationResult().setStatus(ICodingValidationResult.Status.INVALID);
        // todo should I add details
        validateProcessor.getCodingValidationResult().addParameter("information", notCommensurable.diff().entrySet().stream().map(entry -> "%s: %s".formatted(entry.getKey(), entry.getValue())).collect(Collectors.joining(",")));
    }

    private void handleIsCommensurable(RelationCheckerService.IsCommensurable isCommensurable, boolean includeBlock, ValidateProcessor validateProcessor) {
        ICodingValidationResult.Status status = includeBlock ? ICodingValidationResult.Status.VALID : ICodingValidationResult.Status.INVALID;
        validateProcessor.getCodingValidationResult().setStatus(status);
    }

    private List<UCUMExpression.Term> parseConceptRefsOrThrowIfFailed(List<ValueSet.ConceptReferenceComponent> conceptRefs, ValidateProcessor validateProcessor) {
        Map<ValueSet.ConceptReferenceComponent, ValidatorService.ValidationResult> parsedConceptRefsResult = conceptRefs.stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                conceptRef -> ucumService.validate(conceptRef.getCode())
                        )
                );
        List<UCUMExpression.Term> parsedConceptRefs = new ArrayList<>();
        parsedConceptRefsResult.forEach((conceptReferenceComponent, validationResult) -> {
            switch (validationResult) {
                case ValidatorService.Failure failure -> throw new Unchecked.UncheckedUnprocessableEntityException("'%s' is not a valid code.".formatted(conceptReferenceComponent.getCode()), plugin);
                case ValidatorService.Success success -> parsedConceptRefs.add(success.term());
            }
        });
        return parsedConceptRefs;
    }

    private RelationCheckerService.IsEqual checkEquality(Coding originalCoding, UCUMExpression.Term parsedTerm, List<UCUMExpression.Term> parsedConceptRefs) {
        List<RelationCheckerService.IsEqual> equalMatches = parsedConceptRefs.stream()
                .map(parsedConceptRef -> ucumService.checkRelation(parsedTerm, parsedConceptRef))
                .filter(RelationCheckerService.IsEqual.class::isInstance)
                .map(RelationCheckerService.IsEqual.class::cast)
                .toList();
        if(equalMatches.isEmpty()) {
            return null;
        }
        // prefer strict equality to equality after processing
        for(RelationCheckerService.IsEqual equalMatch : equalMatches) {
            if(equalMatch.strictEqual()) {
                return equalMatch;
            }
        }
        for(RelationCheckerService.IsEqual equalMatch : equalMatches) {
            if(equalMatch.equalAfterProcessing()) {
                return equalMatch;
            }
        }
        throw new RuntimeException("If instance of IsEqual, then one parameter has to be true.");
    }

    private List<ValueSet.ConceptReferenceComponent> getConceptRefs(List<ValueSet.ConceptSetComponent> concepts) {
        return concepts.stream()
                .filter(concept -> concept.hasConcept() && UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(concept.getSystem()))
                .flatMap(concept -> concept.getConcept().stream())
                .toList();
    }
}
