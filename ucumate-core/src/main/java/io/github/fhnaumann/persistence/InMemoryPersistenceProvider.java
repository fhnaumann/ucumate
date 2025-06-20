package io.github.fhnaumann.persistence;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Felix Naumann
 */
public class InMemoryPersistenceProvider implements PersistenceProvider, InMemory {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryPersistenceProvider.class);

    private Cache<CanonKey, Canonicalizer.CanonicalStepResult> canonCache;
    private Cache<ValKey, Validator.ValidationResult> valCache;

    private boolean enabled;

    public InMemoryPersistenceProvider(boolean enabled) {
        this.enabled = enabled;
    }

    public InMemoryPersistenceProvider(int canonCacheMaxSize, int valCacheMaxSize, boolean recordStats) {
        if(recordStats) {
            canonCache = Caffeine.newBuilder().maximumSize(canonCacheMaxSize).recordStats().build();
            valCache = Caffeine.newBuilder().maximumSize(valCacheMaxSize).recordStats().build();
        }
        else {
            canonCache = Caffeine.newBuilder().maximumSize(canonCacheMaxSize).build();
            valCache = Caffeine.newBuilder().maximumSize(valCacheMaxSize).build();
        }
    }

    public void preHeat(List<String> ucumCodes) {
        ucumCodes.forEach(code -> {
            Validator.ValidationResult valResult = UCUMService.validate(code);
            switch (valResult) {
                case Validator.Failure failure -> {
                    logger.debug("Preheated {}: Result: invalid. Skipping canonicalization preheat.", code);
                }
                case Validator.Success success -> {
                    logger.debug("Preheated {}: Result: valid", code);
                    Canonicalizer.CanonicalizationResult canonResult = UCUMService.canonicalize(success.term());
                    switch (canonResult) {
                        case Canonicalizer.FailedCanonicalization failedCanonicalization -> logger.debug("Tried to preheat {} for canonicalization but it failed.", code);
                        case Canonicalizer.Success canonSuccess -> logger.debug("Preheated {} for canonicalization.", code);
                    }
                }
            }
        });
        logger.info("Preheated cache from {} codes.", ucumCodes.size());
    }

    @Override
    public void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value) {
        if(isEnabled()) {
            canonCache.put(key, value);
            if(logger.isDebugEnabled()) {
                logger.debug("Saved key={} in cache.", UCUMService.print(key.expression())); // call to #print is expensive here
            }
        }
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(CanonKey key) {
        if(isEnabled()) {
            return canonCache.getIfPresent(key);
        }
        else {
            return null;
        }
    }

    @Override
    public Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        return canonCache.asMap();
    }

    @Override
    public void saveValidated(ValKey key, Validator.ValidationResult value) {
        if(isEnabled()) {
            valCache.put(key, value);
            logger.debug("Saved key={} in cache.", key);
        }
    }

    @Override
    public Validator.ValidationResult getValidated(ValKey key) {
        if(isEnabled()) {
            return valCache.getIfPresent(key);
        }
        else {
            return null;
        }
    }

    @Override
    public Map<ValKey, Validator.ValidationResult> getAllValidated() {
        return valCache.asMap();
    }

    @Override
    public void close() {
        // No-op
    }

    public void clearCache() {
        if(canonCache != null) {
            canonCache.invalidateAll();
        }
        if(valCache != null) {
            valCache.invalidateAll();
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
