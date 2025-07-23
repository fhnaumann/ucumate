package io.github.fhnaumann;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.persistence.InMemory;
import io.github.fhnaumann.persistence.PersistenceProvider;
import io.github.fhnaumann.util.PreciseDecimal;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Felix Naumann
 */
@Service
public class InfinispanPersistenceProvider implements PersistenceProvider, InMemory {

    public static final String CANONICAL_CACHE_NAME = "ucumate-canonical-cache";
    public static final String VALIDATION_CACHE_NAME = "ucumate-validation-cache";
    private static final Logger log = LoggerFactory.getLogger(InfinispanPersistenceProvider.class);

    private final Cache<CanonKey, Canonicalizer.CanonicalStepResult> canonCache;
    private final Cache<ValKey, ValidatorService.ValidationResult> valCache;
    private boolean enabled = true;

    private final PrinterService printerService = new Printer();
    private final ValidatorService validatorService = new Validator();
    private final CanonicalizerService canonicalizerService = new Canonicalizer(printerService, validatorService);

    @Autowired
    public InfinispanPersistenceProvider(DefaultCacheManager cacheManager) {
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

    public void preheat(Collection<String> codes) {
        codes.forEach(code -> {
            Validator.ValidationResult valResult = validatorService.validate(code);
            switch (valResult) {
                case Validator.Failure failure -> {
                    log.debug("Preheated {}: Result: invalid. Skipping canonicalization preheat.", code);
                }
                case Validator.Success success -> {
                    log.debug("Preheated {}: Result: valid", code);
                    CanonicalizerService.CanonicalizationResult canonResult = canonicalizerService.canonicalize(success.term());
                    switch (canonResult) {
                        case CanonicalizerService.FailedCanonicalization failedCanonicalization -> log.debug("Tried to preheat {} for canonicalization but it failed.", code);
                        case CanonicalizerService.Success canonSuccess -> log.debug("Preheated {} for canonicalization.", code);
                    }
                }
            }
        });
        log.info("Preheated Infinispan cache with {} codes.", codes.size());
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
            if(log.isDebugEnabled()) {
                log.debug("Saved key={}, value={} in cache.", printerService.print(key.expression()), value); // call to #print is expensive here
            }
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
            if(log.isDebugEnabled()) {
                log.debug("Saved key={}, value ={}in cache.", key, value);
            }
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
    public void close() {
        canonCache.stop();
        valCache.stop();
    }
}
