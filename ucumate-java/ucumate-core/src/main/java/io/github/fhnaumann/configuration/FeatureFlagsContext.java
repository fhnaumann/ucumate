package io.github.fhnaumann.configuration;

/**
 * @author Felix Naumann
 */
public final class FeatureFlagsContext {

    private static volatile FeatureFlags current = FeatureFlags.none();

    private FeatureFlagsContext() {}

    public static FeatureFlags get() {
        return current;
    }

    public static void set(FeatureFlags flags) {
        current = flags;
    }
}

