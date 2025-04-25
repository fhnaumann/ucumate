package org.example;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.funcs.PrettyPrinter;
import org.example.funcs.UnitExtractor;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UCUMRegistry {

    private static final UCUMRegistry instance = loadFromUCUMEssence(UCUMRegistry.class.getClassLoader().getResourceAsStream("ucum-essence.xml"));

    static {
        // instance.translateValueDefinitions();
    }

    private final UCUMDefinition.UCUMEssence ucumEssence;
    private final Map<String, UCUMDefinition.UCUMPrefix> prefixes;
    private final Map<String, UCUMDefinition.BaseUnit> baseUnits;
    private final Map<String, UCUMDefinition.DefinedUnit> definedUnits;

    private final Map<UCUMDefinition.DefinedUnit, Expression.Term> definedUnitSourceDefinitions;

    private UCUMRegistry(UCUMDefinition.UCUMEssence ucumEssence) {
        this.ucumEssence = ucumEssence;
        this.prefixes = identityFromList(ucumEssence.prefixes());
        this.baseUnits = identityFromList(ucumEssence.baseUnits());
        this.definedUnits = identityFromList(ucumEssence.definedUnits());
        this.definedUnitSourceDefinitions = new HashMap<>();
    }

    private Expression.Term translateUnitInsideDefinedUnitToTerm(UCUMDefinition.DefinedUnit definedUnit) {
        return switch(definedUnit) {
            case UCUMDefinition.DerivedUnit _, UCUMDefinition.DimlessUnit _, UCUMDefinition.ArbitraryUnit _ -> handleCommon(definedUnit);
            case UCUMDefinition.SpecialUnit _ -> {
                Expression.Term term = (Expression.Term) Main.visitTerm(definedUnit.value().function().unit());
                Expression.Term extracted = new UnitExtractor().extractUnits(term);
                PrettyPrinter pp = new PrettyPrinter(true, false, false);
                System.out.println(pp.print(term) + " is extracted to " + pp.print(extracted));
                yield extracted;
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

    private Expression.Term handleCommon(UCUMDefinition.DefinedUnit definedUnit) {
        return (Expression.Term) Main.visitTerm(definedUnit.value().unit());
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

    public Expression.Term getDefinedUnitSourceDefinition(UCUMDefinition.DefinedUnit definedUnit) {
        Expression.Term term = definedUnitSourceDefinitions.get(definedUnit);
        if(term != null) {
            return term;
        }
        term = translateUnitInsideDefinedUnitToTerm(definedUnit);
        //System.out.println("translated " + definedUnit.code() + " to " + new PrettyPrinter(false, false, false).print(term));
        definedUnitSourceDefinitions.put(definedUnit, term);
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
            System.out.println(ucumEssenceXML);
            // Setting the flag in the module is necessary
            // https://github.com/FasterXML/jackson-dataformat-xml/issues/219#issuecomment-286003056
            JacksonXmlModule module = new JacksonXmlModule();
            module.setDefaultUseWrapper(false);
            XmlMapper mapper = new XmlMapper(module);
            // mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
            // mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            UCUMDefinition.UCUMEssence ucumEssence = mapper.readValue(ucumEssenceXML, UCUMDefinition.UCUMEssence.class);
            return new UCUMRegistry(ucumEssence);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static UCUMRegistry getInstance() {
        if(instance.definedUnitSourceDefinitions == null) {
            // getInstance() is being called after the ucum-essence.xml has been read, but no connection between the ucum-terms
            // INSIDE value definitions has been made yet, do this once now
        }
        return instance;
    }

}
