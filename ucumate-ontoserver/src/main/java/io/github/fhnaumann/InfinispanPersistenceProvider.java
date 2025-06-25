package io.github.fhnaumann;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.persistence.InMemory;
import io.github.fhnaumann.persistence.PersistenceProvider;

import java.util.Map;

/**
 * @author Felix Naumann
 */
public class InfinispanPersistenceProvider implements PersistenceProvider, InMemory {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void clearCache() {

    }

    @Override
    public void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value) {

    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(CanonKey key) {
        return null;
    }

    @Override
    public Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        return Map.of();
    }

    @Override
    public void saveValidated(ValKey key, Validator.ValidationResult value) {

    }

    @Override
    public Validator.ValidationResult getValidated(ValKey key) {
        return null;
    }

    @Override
    public Map<ValKey, Validator.ValidationResult> getAllValidated() {
        return Map.of();
    }

    @Override
    public void close() {

    }
}
