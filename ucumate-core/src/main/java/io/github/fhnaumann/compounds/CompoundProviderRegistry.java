package io.github.fhnaumann.compounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ServiceLoader;

/**
 * @author Felix Naumann
 */
public class CompoundProviderRegistry implements CompoundProvider {

    private static final Logger log = LoggerFactory.getLogger(CompoundProviderRegistry.class);
    private static volatile CompoundProvider instance = loadProvider();

    private CompoundProviderRegistry() {}

    private static CompoundProvider loadProvider() {
        return ServiceLoader.load(CompoundProvider.class)
                .findFirst()
                .orElse(new NoOpCompoundProvider());
    }

    public static CompoundProvider get() {
        return instance;
    }

    public static void registerCustom(CompoundProvider customProvider) {
        log.debug("Registering custom compound provider {}.", customProvider.getClass().getSimpleName());
        instance = customProvider;
    }

    @Override
    public String findByName(String name) {
        return instance.findByName(name);
    }

    @Override
    public String findBySynonym(String synonym) {
        return instance.findBySynonym(synonym);
    }

    @Override
    public String findByFormular(String formular) {
        return instance.findByFormular(formular);
    }

    @Override
    public String findByCasRn(String casRn) {
        return instance.findByCasRn(casRn);
    }

    @Override
    public String findByInchiKey(String inchiKey) {
        return instance.findByInchiKey(inchiKey);
    }

    @Override
    public String findByMatch(String value) {
        return instance.findByMatch(value);
    }

    private static class NoOpCompoundProvider implements CompoundProvider {

        @Override
        public String findByName(String name) {
            return null;
        }

        @Override
        public String findBySynonym(String synonym) {
            return null;
        }

        @Override
        public String findByFormular(String formular) {
            return null;
        }

        @Override
        public String findByCasRn(String casRn) {
            return null;
        }

        @Override
        public String findByInchiKey(String inchiKey) {
            return null;
        }

        @Override
        public String findByMatch(String value) {
            return null;
        }
    }
}
