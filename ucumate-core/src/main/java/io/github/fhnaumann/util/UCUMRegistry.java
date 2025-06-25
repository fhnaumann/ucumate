package io.github.fhnaumann.util;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.funcs.printer.PrettyPrinter;
import io.github.fhnaumann.model.UCUMExpression;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.fhnaumann.model.UcumVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UCUMRegistry implements IUCUMRegistry {

    // private static final UCUMRegistry instance = loadFromUCUMEssence(UCUMRegistry.class.getClassLoader().getResourceAsStream(UCUM_ESSENCE_FILE_STRING));
    private static final Logger log = LoggerFactory.getLogger(UCUMRegistry.class);

    private static final UCUMRegistry instance = new UCUMRegistry();

    private static final UcumVersion DEFAULT_UCUM_VERSION = UcumVersion.fromVersionString(ConfigurationRegistry.get().getDefaultUCUMVersion());
    private static final Map<UcumVersion, VersionSpecificUCUMRegistry> registries = initRegistries();

    private static Map<UcumVersion, VersionSpecificUCUMRegistry> initRegistries() {
        List<String> supportedVersions = ConfigurationRegistry.get().getSupportedUCUMVersions();
        return supportedVersions.stream()
                .map(UcumVersion::fromVersionString)
                .collect(Collectors.toMap(
                        Function.identity(),
                        VersionSpecificUCUMRegistry::new
                ));
    }

    /*
    private final UCUMDefinition.UCUMEssence ucumEssence;
    private final Map<String, UCUMDefinition.UCUMPrefix> prefixes;
    private final Map<String, UCUMDefinition.BaseUnit> baseUnits;
    private final Map<String, UCUMDefinition.DefinedUnit> definedUnits;

    private final Map<UCUMDefinition.DefinedUnit, UCUMExpression.Term> definedUnitSourceDefinitions;

     */

    private UCUMRegistry() {

    }

    public VersionSpecificUCUMRegistry getVersionSpecificUCUMRegistry(UcumVersion version) {
        return getOrThrow(version);
    }

    public List<UCUMDefinition.Concept> getAll() {
        return getAll(DEFAULT_UCUM_VERSION);
    }

    public List<UCUMDefinition.Concept> getAll(UcumVersion version) {
        return getOrThrow(version).getAll();
    }

    private VersionSpecificUCUMRegistry getOrThrow(UcumVersion version) {
        VersionSpecificUCUMRegistry versionSpecificUCUMRegistry = registries.get(version);
        if(versionSpecificUCUMRegistry == null) {
            return LogUtil.logAndThrow(log, "Could not get UCUMRegistry for UCUM version {}", version);
        }
        return versionSpecificUCUMRegistry;
    }

    public Collection<UCUMDefinition.UCUMPrefix> getPrefixes(UcumVersion version) {
        return getOrThrow(version).getPrefixes();
    }

    public Collection<UCUMDefinition.UCUMPrefix> getPrefixes(String version) {
        return getPrefixes(UcumVersion.fromVersionString(version));
    }

    public Collection<UCUMDefinition.UCUMPrefix> getPrefixes() {
        return getPrefixes(DEFAULT_UCUM_VERSION);
    }

    public Collection<UCUMDefinition.BaseUnit> getBaseUnits() {
        return getBaseUnits(DEFAULT_UCUM_VERSION);
    }

    public Collection<UCUMDefinition.BaseUnit> getBaseUnits(UcumVersion version) {
        return getOrThrow(version).getBaseUnits();
    }

    public Collection<UCUMDefinition.DefinedUnit> getDefinedUnits() {
        return getDefinedUnits(DEFAULT_UCUM_VERSION);
    }

    public Collection<UCUMDefinition.DefinedUnit> getDefinedUnits(UcumVersion version) {
        return getOrThrow(version).getDefinedUnits();
    }

    public Optional<UCUMDefinition.UCUMUnit> getUCUMUnit(String unit) {

    }

    public Optional<UCUMDefinition.UCUMPrefix> getPrefix(String prefix) {
        return ;
    }

    public Optional<UCUMDefinition.BaseUnit> getBaseUnit(String baseUnit) {
        return Optional.ofNullable(baseUnits.get(baseUnit));
    }

    public Optional<UCUMDefinition.DefinedUnit> getDefinedUnit(String definedUnit) {
        return Optional.ofNullable(definedUnits.get(definedUnit));
    }

    public UCUMExpression.Term getDefinedUnitSourceDefinition(UCUMDefinition.DefinedUnit definedUnit, boolean enableMolarMassConversion) {

    }

    public Optional<UCUMDefinition.Concept> getConcept(String concept) {

    }

    public void warmup() {
    }

    public static UCUMRegistry getInstance() {
        return instance;
    }

}
