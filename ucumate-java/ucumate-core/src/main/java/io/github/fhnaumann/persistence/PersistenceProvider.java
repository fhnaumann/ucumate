package io.github.fhnaumann.persistence;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.ValidatorService;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public interface PersistenceProvider {
     void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value);
     Canonicalizer.CanonicalStepResult getCanonical(CanonKey key);
     Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical();

     void saveValidated(ValKey key, ValidatorService.ValidationResult value);
     ValidatorService.ValidationResult getValidated(ValKey key);
     Map<ValKey, ValidatorService.ValidationResult> getAllValidated();

     Stream<Map.Entry<ValKey, ValidatorService.ValidationResult>> getAllValidatedLazy();

     void close();
}
