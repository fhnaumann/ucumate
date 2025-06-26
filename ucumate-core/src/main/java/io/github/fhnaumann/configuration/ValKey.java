package io.github.fhnaumann.configuration;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;

/**
 * @author Felix Naumann
 */
public record ValKey(String expression, FeatureFlags flags, UcumVersion version) {

    public String toStorageKey(FeatureFlags flags) {
        return FeatureFlags.toStorageKey(expression, flags, version);
    }

    public static ValKey fromStorageKey(String storageKey) {
        return FeatureFlags.fromStorageKey(storageKey);
    }

    public static ValKey of(String expression, UcumVersion version) {
        return new ValKey(expression, FeatureFlagsContext.get(), version);
    }
}
