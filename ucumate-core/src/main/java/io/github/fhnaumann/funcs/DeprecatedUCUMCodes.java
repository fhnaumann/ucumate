package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Felix Naumann
 */
public class DeprecatedUCUMCodes {

    /*
    Use type DefinedUnit instead of more broader UCUMUnit in the hopes that base units are never deprecated.
     */
    private final Map<UCUMDefinition.DefinedUnit, DeprecationInfo> deprecatedCodes;

    private final UCUMService service;
    private final VersionSpecificUCUMRegistry registry;
    private final TermFolder termFolder;

    public DeprecatedUCUMCodes(UCUMService service) {
        this.service = service;
        this.registry = service.getUCUMRegistry();
        this.termFolder = new TermFolder(service);
        this.deprecatedCodes = createDeprecatedCodes();
    }

    private Map<UCUMDefinition.DefinedUnit, DeprecationInfo> createDeprecatedCodes() {
        /*
        Fingers crossed, hopefully this list stays version-independent
         */
        Map<UCUMDefinition.DefinedUnit, DeprecationInfo> map = new HashMap<>();
        map.put(get("[ppb]"), of("\"billion\" is ambiguous in different languages.", "10*-9", "ug/kg"));
        map.put(get("[pptr]"), of("\"trillion\" is ambiguous in different languages.", "10*-12"));
        String dilutionWarning = "Dilution functions may cause astronomical values, leading to overflow conditions.";
        map.put(get("[hp'_X]"), of(dilutionWarning, "[hp_X]"));
        map.put(get("[hp'_C]"), of(dilutionWarning, "[hp_C]"));
        map.put(get("[hp'_M]"), of(dilutionWarning, "[hp_M]"));
        map.put(get("[hp'_Q]"), of(dilutionWarning, "[hp_Q]"));
        return map;
    }

    public List<DeprecationInfo> getDeprecated(UCUMExpression.Term term) {
        return termFolder.extractFrom(term, Function.identity()).stream()
                .filter(UCUMDefinition.DefinedUnit.class::isInstance)
                .map(UCUMDefinition.DefinedUnit.class::cast)
                .map(this::getDeprecated)
                .flatMap(Optional::stream)
                .toList();
    }

    public Optional<DeprecationInfo> getDeprecated(UCUMDefinition.DefinedUnit unit) {
        return Optional.ofNullable(deprecatedCodes.get(unit));
    }

    private UCUMDefinition.DefinedUnit get(String unit) {
        return registry.getDefinedUnit(unit).orElseThrow(() -> new IllegalStateException("Failed to get %s from registry with version %s.".formatted(unit, service.getUCUMVersion())));
    }

    private DeprecationInfo of(String reason, String... alternatives) {
        return new DeprecationInfo(reason, List.of(alternatives));
    }

    public record DeprecationInfo(String reason, List<String> alternatives) {}
}
