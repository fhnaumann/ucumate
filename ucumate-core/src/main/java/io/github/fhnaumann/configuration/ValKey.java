package io.github.fhnaumann.configuration;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public record ValKey(String expression, FeatureFlags flags) {

    public String toStorageKey(FeatureFlags flags) {
        return FeatureFlags.toStorageKey(expression, flags);
    }

    public static ValKey fromStorageKey(String storageKey) {
        return FeatureFlags.fromStorageKey(storageKey);
    }

    public static ValKey of(String expression) {
        return new ValKey(expression, FeatureFlagsContext.get());
    }
}
