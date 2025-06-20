package io.github.fhnaumann.util;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.funcs.printer.PrettyPrinter;
import io.github.fhnaumann.model.UCUMExpression;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UCUMRegistry {

    private static final UCUMRegistry instance = loadFromUCUMEssence(UCUMRegistry.class.getClassLoader().getResourceAsStream("ucum-essence.xml"));
    private static final Logger log = LoggerFactory.getLogger(UCUMRegistry.class);

    static {
        // instance.translateValueDefinitions();
    }

    private final UCUMDefinition.UCUMEssence ucumEssence;
    private final Map<String, UCUMDefinition.UCUMPrefix> prefixes;
    private final Map<String, UCUMDefinition.BaseUnit> baseUnits;
    private final Map<String, UCUMDefinition.DefinedUnit> definedUnits;

    private final Map<UCUMDefinition.DefinedUnit, UCUMExpression.Term> definedUnitSourceDefinitions;

    private UCUMRegistry(UCUMDefinition.UCUMEssence ucumEssence) {
        this.ucumEssence = ucumEssence;
        this.prefixes = identityFromList(ucumEssence.prefixes());
        this.baseUnits = identityFromList(ucumEssence.baseUnits());
        this.definedUnits = identityFromList(ucumEssence.definedUnits());
        this.definedUnitSourceDefinitions = new HashMap<>();
    }

    public List<UCUMDefinition.Concept> getAll() {
        List<UCUMDefinition.Concept> all = new ArrayList<>();
        all.addAll(prefixes.values());
        all.addAll(baseUnits.values());
        all.addAll(definedUnits.values());
        return all;
    }

    public Collection<UCUMDefinition.UCUMPrefix> getPrefixes() {
        return prefixes.values();
    }

    public Collection<UCUMDefinition.BaseUnit> getBaseUnits() {
        return baseUnits.values();
    }

    public Collection<UCUMDefinition.DefinedUnit> getDefinedUnits() {
        return definedUnits.values();
    }

    private UCUMExpression.Term translateUnitInsideDefinedUnitToTerm(UCUMDefinition.DefinedUnit definedUnit) {
        return switch(definedUnit) {
            case UCUMDefinition.DerivedUnit derivedUnit -> handleCommon(definedUnit);
            case UCUMDefinition.DimlessUnit dimlessUnit -> handleCommon(definedUnit);
            case UCUMDefinition.ArbitraryUnit arbitraryUnit -> handleCommon(definedUnit);
            case UCUMDefinition.SpecialUnit specialUnit -> {
                /*
                The definition of special units are UCUM expressions themselves. However, some information are redundant
                because factors are already accounted for in each special unit case. Therefore, we are really only
                interested in the units (but not any factors) in the definition. That's what the extractor is used for.
                Here are some examples:
                Degree is defined as "K" -> Keep as is
                Fahrenheit is defined as "K/9" -> Only keep "K" because the "/9" is already accounted for manually in the conversion

                 */
                UCUMExpression.Term term = ((Validator.Success) UCUMService.validate(definedUnit.value().function().unit())).term();
                //UCUMExpression.Term extracted = new UnitExtractor().extractUnits(term);
                PrettyPrinter pp = new PrettyPrinter();
                //System.out.println(pp.print(term) + " is extracted to " + pp.print(extracted));
                yield term;
                // canonicalization necessary because the UCUM definition uses a term here, which is already covered in the specialfunction.
                // Here we are only interested in the dimension of the special unit
                /*Canonicalizer.CanonicalizationResult result = new Canonicalizer().canonicalize(term, new Canonicalizer.SpecialUnitConversionContext(PreciseDecimal.ONE, Canonicalizer.SpecialUnitApplicationDirection.NO_SPECIAL_INVOLVED));
                yield switch(result) {
                    case Canonicalizer.Success(
                            PreciseDecimal _, Expression.Term term1,
                            Canonicalizer.SpecialUnitApplicationDirection _
                                               ) -> term1;
                    case Canonicalizer.FailedCanonicalization failedCanonicalization -> throw new RuntimeException("CATASTROPHIC, IMPROVE ERROR HANDLING");
                };

                 */
                //yield term;
            }
        };
    }

    private UCUMExpression.Term handleCommon(UCUMDefinition.DefinedUnit definedUnit) {
        return ((Validator.Success) UCUMService.validate(definedUnit.value().unit())).term();
    }

    public Optional<UCUMDefinition.UCUMUnit> getUCUMUnit(String unit) {
        Optional<UCUMDefinition.BaseUnit> optionalBaseUnit = getBaseUnit(unit);
        if(optionalBaseUnit.isPresent()) {
            return Optional.of(optionalBaseUnit.get());
        }
        Optional<UCUMDefinition.DefinedUnit> optionalDefinedUnit = getDefinedUnit(unit);
        if(optionalDefinedUnit.isPresent()) {
            return Optional.of(optionalDefinedUnit.get());
        }
        return Optional.empty();
    }

    public Optional<UCUMDefinition.UCUMPrefix> getPrefix(String prefix) {
        return Optional.ofNullable(prefixes.get(prefix));
    }

    public Optional<UCUMDefinition.BaseUnit> getBaseUnit(String baseUnit) {
        return Optional.ofNullable(baseUnits.get(baseUnit));
    }

    public Optional<UCUMDefinition.DefinedUnit> getDefinedUnit(String definedUnit) {
        return Optional.ofNullable(definedUnits.get(definedUnit));
    }

    public UCUMExpression.Term getDefinedUnitSourceDefinition(UCUMDefinition.DefinedUnit definedUnit, boolean enableMolarMassConversion) {
        UCUMExpression.Term term = definedUnitSourceDefinitions.get(definedUnit);
        if(term == null) {
            term = translateUnitInsideDefinedUnitToTerm(definedUnit);
            //System.out.println("translated " + definedUnit.code() + " to " + new PrettyPrinter(false, false, false).print(term));
            definedUnitSourceDefinitions.put(definedUnit, term);
        }
        if(definedUnit.code().equals("mol") && (enableMolarMassConversion && ConfigurationRegistry.get().isEnableMolMassConversion())) {
            log.debug("Changed the definition of mol (dimless) to have to point to g (mass). The required substance's molar mass has to be provided from the outer scope.");
            /*
            If the mol unit requested and mol<->mass conversion is enabled, then convert mol to gram instead.
            The substance's molar mass has to be provided from outside.
            mol = X * g, where X is provided by the user
             */
            return SoloTermBuilder.builder().withoutPrefix(getBaseUnit("g").orElseThrow()).noExpNoAnnot().asTerm().build();
        }
        return term;
    }

    public Optional<UCUMDefinition.Concept> getConcept(String concept) {
        UCUMDefinition.Concept prefix = getConceptFrom(concept, prefixes);
        if(prefix != null) {
            return Optional.of(prefix);
        }
        UCUMDefinition.BaseUnit baseUnit = (UCUMDefinition.BaseUnit) getConceptFrom(concept, baseUnits);
        if(baseUnit != null) {
            return Optional.of(baseUnit);
        }
        UCUMDefinition.DefinedUnit definedUnit = (UCUMDefinition.DefinedUnit) getConceptFrom(concept, definedUnits);
        if(definedUnit != null) {
            return Optional.of(definedUnit);
        }
        return Optional.empty();
    }

    private UCUMDefinition.Concept getConceptFrom(String concept, Map<String, ? extends UCUMDefinition.Concept> source) {
        return source.get(concept);
    }

    private static <T extends UCUMDefinition.Concept> Map<String, T> identityFromList(List<T> list) {
        return list.stream().collect(Collectors.toMap(UCUMDefinition.Concept::code, Function.identity()));
    }

    private static UCUMRegistry loadFromUCUMEssence(InputStream ucumEssenceXML) {
        try {
            // Setting the flag in the module is necessary
            // https://github.com/FasterXML/jackson-dataformat-xml/issues/219#issuecomment-286003056
            JacksonXmlModule module = new JacksonXmlModule();
            module.setDefaultUseWrapper(false);
            XmlMapper mapper = new XmlMapper(module);
            mapper.deactivateDefaultTyping();
            mapper.addMixIn(UCUMExpression.class, UCUMDefinition.UCUMExpressionMixIn.class);
            // mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
            // mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            UCUMDefinition.UCUMEssence ucumEssence = mapper.readValue(ucumEssenceXML, UCUMDefinition.UCUMEssence.class);
            return new UCUMRegistry(ucumEssence);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void warmup() {
        prefixes.forEach((s, ucumPrefix) -> getPrefix(s));
        baseUnits.forEach((s, baseUnit) -> getBaseUnit(s));
        definedUnits.forEach((s, definedUnit) -> getDefinedUnit(s));
        definedUnits.forEach((s, definedUnit) -> getDefinedUnitSourceDefinition(definedUnit, ConfigurationRegistry.get().isEnableMolMassConversion()));
    }

    public static UCUMRegistry getInstance() {
        if(instance.definedUnitSourceDefinitions == null) {
            // getInstance() is being called after the ucum-essence.xml has been read, but no connection between the ucum-terms
            // INSIDE value definitions has been made yet, do this once now
        }
        return instance;
    }

}
