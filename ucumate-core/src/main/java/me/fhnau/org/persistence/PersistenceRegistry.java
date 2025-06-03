package me.fhnau.org.persistence;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.model.UCUMExpression;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Felix Naumann
 */
public class PersistenceRegistry implements PersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceRegistry.class);

    private static final PersistenceRegistry INSTANCE = new PersistenceRegistry();

    private PersistenceRegistry() {}

    private static InMemoryCanonicalizePersistenceProvider cache = new InMemoryCanonicalizePersistenceProvider();
    private static final Map<String, PersistenceProvider> additionalProviders = new HashMap<>();


    public static void register(String name, PersistenceProvider provider) {
        additionalProviders.put(name, provider);
    }

    public static void enableInMemoryCache() {
        cache.setEnabled(true);
    }

    public static void disableInMemoryCache(boolean deleteCacheEntries) {
        if(cache == null) {
            return;
        }
        if(deleteCacheEntries) {
            cache.clearCache();
        }
        cache.setEnabled(false);
    }
    public static PersistenceRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
        cache.saveCanonical(key, value);
        additionalProviders.forEach((s, entry) -> entry.saveCanonical(key, value));
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
        Canonicalizer.CanonicalStepResult canonicalStepResult = cache.getCanonical(key);
        if(canonicalStepResult != null) {
            return canonicalStepResult;
        }
        for(Map.Entry<String, PersistenceProvider>  provider : additionalProviders.entrySet()) {
            canonicalStepResult = provider.getValue().getCanonical(key);
            if(canonicalStepResult != null) {
                return canonicalStepResult;
            }
        }
        return null;
    }

    @Override
    public void saveValidated(String key, Validator.ValidationResult value) {
        cache.saveValidated(key, value);
        additionalProviders.forEach((s, entry) -> entry.saveValidated(key, value));
    }

    @Override
    public Validator.ValidationResult getValidated(String key) {
        Validator.ValidationResult validationResult = cache.getValidated(key);
        if(validationResult != null) {
            return validationResult;
        }
        for(Map.Entry<String, PersistenceProvider>  provider : additionalProviders.entrySet()) {
            validationResult = provider.getValue().getValidated(key);
            if(validationResult != null) {
                return validationResult;
            }
        }
        return null;
    }

    @Override
    public void close() {
        additionalProviders.forEach((s, persistenceProvider) -> persistenceProvider.close());
        additionalProviders.clear();
    }
}
