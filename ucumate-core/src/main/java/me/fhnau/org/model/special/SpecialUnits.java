package me.fhnau.org.model.special;

import java.util.ServiceLoader;

public class SpecialUnits {

    private static SpecialUnitsFunctionProvider provider = loadProvider();

    private static SpecialUnitsFunctionProvider loadProvider() {
        // try ServiceLoader
        ServiceLoader<SpecialUnitsFunctionProvider> loader = ServiceLoader.load(SpecialUnitsFunctionProvider.class);
        for (SpecialUnitsFunctionProvider impl : loader) {
            return impl;
        }

        // fall back to default
        return new DefaultSpecialUnitsFunctionProvider();
    }

    public static SpecialUnitsFunctionProvider getProvider() {
        return provider;
    }

    public static void setProvider(SpecialUnitsFunctionProvider customProvider) {
        provider = customProvider != null ? customProvider : loadProvider();
    }

    public static SpecialUnitsFunctionProvider.ConversionFunction getFunction(String name) {
        return provider.getFunction(name);
    }
}

