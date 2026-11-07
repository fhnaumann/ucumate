package io.github.fhnaumann.util;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UCUMRegistry {

    // private static final UCUMRegistry instance = loadFromUCUMEssence(UCUMRegistry.class.getClassLoader().getResourceAsStream(UCUM_ESSENCE_FILE_STRING));
    private static final Logger log = LoggerFactory.getLogger(UCUMRegistry.class);

    private static final UCUMRegistry instance = new UCUMRegistry();

    private static final Map<UcumVersion, VersionSpecificUCUMRegistry> registries = initRegistries();

    private static Map<UcumVersion, VersionSpecificUCUMRegistry> initRegistries() {
        return ConfigurationRegistry.SUPPORTED_UCUM_VERSIONS.stream()
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

    public List<UCUMDefinition.Concept> getAll(UcumVersion version) {
        return getOrThrow(version).getAll();
    }

    public Collection<UCUMDefinition.UCUMUnit> getUCUMUnits(UcumVersion version) {
        return getOrThrow(version).getUCUMUnits();
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

    public Collection<UCUMDefinition.BaseUnit> getBaseUnits(UcumVersion version) {
        return getOrThrow(version).getBaseUnits();
    }

    public Collection<UCUMDefinition.DefinedUnit> getDefinedUnits(UcumVersion version) {
        return getOrThrow(version).getDefinedUnits();
    }

    public Optional<UCUMDefinition.UCUMUnit> getUCUMUnit(String unit, UcumVersion version) {
        return getOrThrow(version).getUCUMUnit(unit);
    }

    public Optional<UCUMDefinition.UCUMPrefix> getPrefix(String prefix, UcumVersion version) {
        return getOrThrow(version).getPrefix(prefix);
    }

    public Optional<UCUMDefinition.BaseUnit> getBaseUnit(String baseUnit, UcumVersion version) {
        return getOrThrow(version).getBaseUnit(baseUnit);
    }

    public Optional<UCUMDefinition.DefinedUnit> getDefinedUnit(String definedUnit, UcumVersion version) {
        return getOrThrow(version).getDefinedUnit(definedUnit);
    }

    public UCUMExpression.Term getDefinedUnitSourceDefinition(UCUMDefinition.DefinedUnit definedUnit, boolean enableMolarMassConversion, UcumVersion version) {
        return getOrThrow(version).getDefinedUnitSourceDefinition(definedUnit, enableMolarMassConversion);
    }

    public Optional<UCUMDefinition.Concept> getConcept(String concept, UcumVersion version) {
        return getOrThrow(version).getConcept(concept);
    }

    public void warmup() {
        registries.values().forEach(VersionSpecificUCUMRegistry::warmup);
    }

    public static UCUMRegistry getInstance() {
        return instance;
    }

}
