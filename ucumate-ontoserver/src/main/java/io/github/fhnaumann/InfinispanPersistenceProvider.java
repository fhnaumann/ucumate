package io.github.fhnaumann;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.persistence.InMemory;
import io.github.fhnaumann.persistence.PersistenceProvider;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.IOException;
import java.util.Map;

/**
 * @author Felix Naumann
 */
public class InfinispanPersistenceProvider implements PersistenceProvider, InMemory {

    public static final String CANONICAL_CACHE_NAME = "ucumate-canonical-cache";
    public static final String VALIDATION_CACHE_NAME = "ucumate-validation-cache";

    private final UcumVersion ucumVersion;
    private final Cache<CanonKey, Canonicalizer.CanonicalStepResult> canonCache;
    private final Cache<ValKey, ValidatorService.ValidationResult> valCache;
    private boolean enabled = true;

    public InfinispanPersistenceProvider(EmbeddedCacheManager cacheManager) {
        this.ucumVersion = UcumVersion.V2_2; // todo figure out why a persistence provider needs a ucum version anyway

        if (!cacheManager.cacheExists(CANONICAL_CACHE_NAME)) {
            cacheManager.defineConfiguration(CANONICAL_CACHE_NAME,
                    new ConfigurationBuilder()
                            .clustering().cacheMode(CacheMode.DIST_SYNC) // or REPL_SYNC
                            .build());
        }

        if (!cacheManager.cacheExists(VALIDATION_CACHE_NAME)) {
            cacheManager.defineConfiguration(VALIDATION_CACHE_NAME,
                    new ConfigurationBuilder()
                            .clustering().cacheMode(CacheMode.DIST_SYNC)
                            .build());
        }

        this.canonCache = cacheManager.getCache(CANONICAL_CACHE_NAME);
        this.valCache = cacheManager.getCache(VALIDATION_CACHE_NAME);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void clearCache() {
        canonCache.clear();
        valCache.clear();
    }

    @Override
    public void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value) {
        if(enabled) {
            canonCache.put(key, value);
        }
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(CanonKey key) {
        return enabled ? canonCache.get(key) : null;
    }

    @Override
    public Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        return Map.copyOf(canonCache);
    }

    @Override
    public void saveValidated(ValKey key, Validator.ValidationResult value) {
        if(enabled) {
            valCache.put(key, value);
        }
    }

    @Override
    public Validator.ValidationResult getValidated(ValKey key) {
        return enabled ? valCache.get(key) : null;
    }

    @Override
    public Map<ValKey, Validator.ValidationResult> getAllValidated() {
        return Map.copyOf(valCache);
    }

    @Override
    public UcumVersion getVersion() {
        return ucumVersion;
    }

    @Override
    public void close() {
        canonCache.stop();
        valCache.stop();
    }
}
