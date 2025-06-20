package io.github.fhnaumann;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.UCUMRegistry;
import org.fhir.ucum.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
public class UcumateToUcumJavaService implements UcumService {

    private static final Logger log = LoggerFactory.getLogger(UcumateToUcumJavaService.class);
    private final UcumEssenceService legacy;
    private final UcumJavaLegacyPrinter legacyPrinter;
    private final UcumJavaCanonicalUnitsLegacyPrinter canonicalUnitsLegacyPrinter;

    public UcumateToUcumJavaService(InputStream stream) throws UcumException {
        legacy = new UcumEssenceService(stream);
        this.legacyPrinter = new UcumJavaLegacyPrinter();
        this.canonicalUnitsLegacyPrinter = new UcumJavaCanonicalUnitsLegacyPrinter();
    }

    public UcumateToUcumJavaService(String filename) throws UcumException {
        legacy = new UcumEssenceService(filename);
        this.legacyPrinter = new UcumJavaLegacyPrinter();
        this.canonicalUnitsLegacyPrinter = new UcumJavaCanonicalUnitsLegacyPrinter();
    }

    @Override
    public UcumModel getModel() {
        return legacy.getModel();
    }

    @Override
    public UcumService.UcumVersionDetails ucumIdentification() {
        return legacy.ucumIdentification();
    }

    @Override
    public List<String> validateUCUM() {
        /*
        Calling the registry instance itself automatically checks for malformed syntax because it will just error out.

        This returns an empty list because it implements all special units.
         */
        UCUMRegistry registry = UCUMRegistry.getInstance();
        return registry.getAll().stream()
                .filter(concept -> !UCUMService.validateToBool(concept.code()))
                .map(concept -> "%s is invalid according to ucumate but exists in the provided essence file.".formatted(concept.code()))
                .toList();
    }

    @Override
    public List<Concept> search(ConceptKind kind, String text, boolean isRegex) {
        Collection<UCUMDefinition.Concept> poolOfConceptsToSearchFrom = new HashSet<>(switch (kind) {
            case PREFIX -> UCUMRegistry.getInstance().getPrefixes();
            case BASEUNIT -> UCUMRegistry.getInstance().getBaseUnits();
            case UNIT -> UCUMRegistry.getInstance().getDefinedUnits();
            case null -> UCUMRegistry.getInstance().getAll();
        });
        return poolOfConceptsToSearchFrom
                .stream()
                .filter(concept -> matchConcept(concept, text, isRegex))
                .map(UcumateToUcumJavaService::fromUcumateConceptToUcumJavaConcept)
                .toList();
    }

    private static boolean matchConcept(UCUMDefinition.Concept concept, String text, boolean isRegex) {
        return concept.names().stream().anyMatch(name -> matches(name, text, isRegex))
                || matches(concept.code(), text, isRegex)
                || matches(concept.codeCaseInsensitive(), text, isRegex)
                || matches(concept.printSymbol(), text, isRegex)
                || concept instanceof UCUMDefinition.UCUMUnit unit && matches(unit.property(), text, isRegex);
    }

    private static Concept fromUcumateConceptToUcumJavaConcept(UCUMDefinition.Concept ucumateConcept) {
        Concept ucumJavaConcept =  switch (ucumateConcept) {
            case UCUMDefinition.UCUMPrefix prefix -> {
                Prefix ucumJavaPrefix = new Prefix(ucumateConcept.code(), ucumateConcept.codeCaseInsensitive());
                try {
                    ucumJavaPrefix.setValue(new Decimal(prefix.value().conversionFactor().toString()));
                    yield ucumJavaPrefix;
                } catch (UcumException e) {
                    throw new RuntimeException(e);
                }
            }
            case UCUMDefinition.BaseUnit baseUnit -> {
                BaseUnit ucumJavaBaseUnit = new BaseUnit(ucumateConcept.code(), ucumateConcept.codeCaseInsensitive());
                // ucum-java uses the first char of the dim string from the essence xml
                ucumJavaBaseUnit.setDim(baseUnit.dim().charAt(0));
                ucumJavaBaseUnit.setProperty(baseUnit.property());
                yield ucumJavaBaseUnit;
            }
            case UCUMDefinition.DefinedUnit definedUnit -> getUcumJavaDefinedUnitFromUcumateDefinedUnit(definedUnit);
        };
        ucumJavaConcept.setPrintSymbol(ucumateConcept.printSymbol());
        ucumJavaConcept.getNames().addAll(ucumateConcept.names());
        return ucumJavaConcept;
    }

    private static boolean matches(String value, String text, boolean isRegex) {
        return (value != null) && ((isRegex  && value.matches(text)) || (!isRegex && value.toLowerCase().contains(text.toLowerCase())));
    }

    @Override
    public Set<String> getProperties() {
        return UCUMRegistry.getInstance().getDefinedUnits().stream()
                .map(UCUMDefinition.UCUMUnit::property)
                .collect(Collectors.toSet());
    }

    @Override
    public String validate(String unit) {
        return UCUMService.validateToBool(unit) ? null : "ucumate found the input to be invalid.";
    }

    @Override
    public String analyse(String unit) throws UcumException {
        return UCUMService.print(unit, legacyPrinter);
    }

    @Override
    public String validateInProperty(String unit, String property) {
        throw new UnsupportedOperationException("validateInProperty is not yet implemented in the bridge.");
    }

    @Override
    public String validateCanonicalUnits(String unit, String canonical) {
        Canonicalizer.CanonicalizationResult canonResult = UCUMService.canonicalize(unit);
        if(!(canonResult instanceof Canonicalizer.Success success)) {
            return "%s is not a valid unit.".formatted(unit);
        }
        Validator.ValidationResult canonicalValResult = UCUMService.validate(canonical);
        if(!(canonicalValResult instanceof Validator.Success canonicalParsedSuccess)) {
            return "%s is not a valid unit.".formatted(canonical);
        }
        if(!CanonicalChecker.containsOnlyCanonicalExpressions(canonicalParsedSuccess.term())) {
            return "%s is not a canonical unit.".formatted(canonical);
        }
        // It is safe to canonicalize now because if it is not a canonical unit, the CanonicalChecker would have returned false
        Canonicalizer.CanonicalizationResult canonicalizationResult = UCUMService.canonicalize(canonicalParsedSuccess.term());
        if(!(canonicalizationResult instanceof Canonicalizer.Success success1)) {
            throw new RuntimeException("Canonicalized failed but CanonicalChecker said it should work.");
        }

        DimensionAnalyzer.ComparisonResult comparisonResult = DimensionAnalyzer.compare(success.canonicalTerm(), success1.canonicalTerm());
        return switch (comparisonResult) {
            case DimensionAnalyzer.Success comparisonSuccess -> null;
            case DimensionAnalyzer.Failure failure -> "The provided unit '%s' and the desired canonical form '%s' do not match. This is their difference: %s".formatted(unit, canonical, failure.difference());
        };
    }

    @Override
    public String getCanonicalUnits(String unit) throws UcumException {
        return switch (UCUMService.canonicalize(unit)) {
            case Canonicalizer.FailedCanonicalization failedCanonicalization -> throw new UcumException(failedCanonicalization.toString());
            case Canonicalizer.Success success -> UCUMService.print(success.canonicalTerm(), canonicalUnitsLegacyPrinter);
        };
    }

    @Override
    public boolean isComparable(String units1, String units2) throws UcumException {
        Canonicalizer.CanonicalizationResult units1Result = UCUMService.canonicalize(units1);
        if(!(units1Result instanceof Canonicalizer.Success units1Success)) {
            throw new UcumException("%s could not be canonicalized.".formatted(units1));
        }
        Canonicalizer.CanonicalizationResult units2Result = UCUMService.canonicalize(units2);
        if(!(units2Result instanceof Canonicalizer.Success units2Success)) {
            throw new UcumException("%s could not be canonicalized.".formatted(units2));
        }
        return switch (DimensionAnalyzer.compare(units1Success.canonicalTerm(), units2Success.canonicalTerm())) {
            case DimensionAnalyzer.Failure failure -> false;
            case DimensionAnalyzer.Success success -> true;
        };
    }

    @Override
    public List<DefinedUnit> getDefinedForms(String code) throws UcumException {
        Optional<UCUMDefinition.BaseUnit> optBaseUnit = UCUMRegistry.getInstance().getBaseUnit(code);
        if (optBaseUnit.isEmpty()) {
            return List.of();
        }
        UCUMDefinition.BaseUnit baseUnit = optBaseUnit.get();
        UCUMExpression.CanonicalTerm baseUnitAsTerm = (UCUMExpression.CanonicalTerm) SoloTermBuilder.builder().withoutPrefix(baseUnit).noExpNoAnnot().asTerm().build();

        List<UCUMDefinition.DefinedUnit> result = new ArrayList<>();
        for (UCUMDefinition.DefinedUnit definedUnit : UCUMRegistry.getInstance().getDefinedUnits()) {
            var term = SoloTermBuilder.builder()
                    .withoutPrefix(definedUnit)
                    .noExpNoAnnot()
                    .asTerm()
                    .build();

            var canonResult = UCUMService.canonicalize(term);
            if (!(canonResult instanceof Canonicalizer.Success success)) {
                log.error("Encountered canonicalization error for unit {}.", definedUnit);
                continue;
            }
            var comparison = DimensionAnalyzer.compare(success.canonicalTerm(), baseUnitAsTerm);
            if (comparison instanceof DimensionAnalyzer.Success) {
                result.add(definedUnit);
            }
        }
        return result.stream()
                .map(UcumateToUcumJavaService::getUcumJavaDefinedUnitFromUcumateDefinedUnit)
                .toList();
    }

    private static DefinedUnit getUcumJavaDefinedUnitFromUcumateDefinedUnit(UCUMDefinition.DefinedUnit definedUnit) {
        DefinedUnit fhirDefinedUnit = new DefinedUnit(definedUnit.code(), definedUnit.codeCaseInsensitive());
        fhirDefinedUnit.setMetric(definedUnit.isMetric());
        fhirDefinedUnit.setSpecial(definedUnit instanceof UCUMDefinition.SpecialUnit);
        fhirDefinedUnit.setProperty(definedUnit.property());
        fhirDefinedUnit.setPrintSymbol(definedUnit.printSymbol());
        fhirDefinedUnit.getNames().addAll(definedUnit.names());
        try {
            Value value = new Value(definedUnit.value().unit(), definedUnit.value().unitAlt(), new Decimal(definedUnit.value().conversionFactor().toString()));
            value.setText(value.getValue().asDecimal());
            fhirDefinedUnit.setValue(value);
            return fhirDefinedUnit;
        } catch (UcumException e) {
            return null;
        }
    }

    @Override
    public Pair getCanonicalForm(Pair value) throws UcumException {
        return switch (UCUMService.canonicalize(value.getValue().asDecimal(), value.getCode())) {
            case Canonicalizer.FailedCanonicalization failedCanonicalization -> throw new UcumException(failedCanonicalization.toString());
            case Canonicalizer.Success success -> new Pair(new Decimal(success.magnitude().toString()), UCUMService.print(success.canonicalTerm(), Printer.PrintType.UCUM_SYNTAX));
        };
    }

    @Override
    public Decimal convert(Decimal value, String sourceUnit, String destUnit) throws UcumException {
        return switch (UCUMService.convert(value.asDecimal(), sourceUnit, destUnit)) {
            case Converter.FailedConversion failedConversion -> throw new UcumException(failedConversion.toString());
            case Converter.Success success -> new Decimal(success.conversionFactor().toString());
        };
    }

    @Override
    public Pair multiply(Pair o1, Pair o2) throws UcumException {
        throw new UnsupportedOperationException("Multiplying is not supported in ucumate.");
    }

    @Override
    public Pair divideBy(Pair dividend, Pair divisor) throws UcumException {
        throw new UnsupportedOperationException("Dividing is not supported in ucumate.");
    }

    @Override
    public String getCommonDisplay(String code) {
        return legacy.getCommonDisplay(code);
    }
}
