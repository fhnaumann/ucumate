package io.github.fhnaumann.persistence;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UcumVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class InMemoryPersistenceProvider implements PersistenceProvider, InMemory {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryPersistenceProvider.class);

    private final UcumVersion ucumVersion;

    private Cache<CanonKey, Canonicalizer.CanonicalStepResult> canonCache;
    private Cache<ValKey, Validator.ValidationResult> valCache;

    private final ValidatorService validatorService = new Validator();
    private final PrinterService printerService = new Printer();
    private final CanonicalizerService canonicalizerService = new Canonicalizer(printerService, validatorService);

    private boolean enabled;

    public InMemoryPersistenceProvider(UcumVersion ucumVersion, int canonCacheMaxSize, int valCacheMaxSize, boolean recordStats) {
        this.ucumVersion = ucumVersion;
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
            Validator.ValidationResult valResult = validatorService.validate(code);
            switch (valResult) {
                case Validator.Failure failure -> {
                    logger.debug("Preheated {}: Result: invalid. Skipping canonicalization preheat.", code);
                }
                case Validator.Success success -> {
                    logger.debug("Preheated {}: Result: valid", code);
                    CanonicalizerService.CanonicalizationResult canonResult = canonicalizerService.canonicalize(success.term());
                    switch (canonResult) {
                        case CanonicalizerService.FailedCanonicalization failedCanonicalization -> logger.debug("Tried to preheat {} for canonicalization but it failed.", code);
                        case CanonicalizerService.Success canonSuccess -> logger.debug("Preheated {} for canonicalization.", code);
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
                logger.debug("Saved key={} in cache.", printerService.print(key.expression())); // call to #print is expensive here
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
            logger.debug("Saved key = {}, value = {} in cache.", key, value);
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
    public Stream<Map.Entry<ValKey, ValidatorService.ValidationResult>> getAllValidatedLazy() {
        return valCache.asMap().entrySet().stream();
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
