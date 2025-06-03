package me.fhnau.org.persistence;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public class InMemoryCanonicalizePersistenceProvider implements PersistenceProvider {

    public static final Cache<UCUMExpression, Canonicalizer.CanonicalStepResult> CANON_CACHE = Caffeine.newBuilder().maximumSize(10_000).recordStats().build();
    public static final Cache<String, Validator.ValidationResult> VAL_CACHE = Caffeine.newBuilder().maximumSize(10_000).recordStats().build();

    private boolean enabled;

    @Override
    public void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
        if(isEnabled()) {
            CANON_CACHE.put(key, value);
        }
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
        if(isEnabled()) {
            return CANON_CACHE.getIfPresent(key);
        }
        else {
            return null;
        }
    }

    @Override
    public void saveValidated(String key, Validator.ValidationResult value) {
        if(isEnabled()) {
            VAL_CACHE.put(key, value);
        }
    }

    @Override
    public Validator.ValidationResult getValidated(String key) {
        if(isEnabled()) {
            return VAL_CACHE.getIfPresent(key);
        }
        else {
            return null;
        }
    }

    @Override
    public void close() {
        // No-op
    }

    public void clearCache() {
        CANON_CACHE.invalidateAll();
        VAL_CACHE.invalidateAll();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
