package io.github.fhnaumann.persistence;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;

import java.util.Map;

/**
 * @author Felix Naumann
 */
public interface PersistenceProvider {
     void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value);
     default void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
          saveCanonical(CanonKey.of(key, getVersion()), value);
     }
     Canonicalizer.CanonicalStepResult getCanonical(CanonKey key);
     default Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
          return getCanonical(CanonKey.of(key, getVersion()));
     }
     Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical();

     void saveValidated(ValKey key, ValidatorService.ValidationResult value);
     default void saveValidated(String key, ValidatorService.ValidationResult value) {
          saveValidated(ValKey.of(key, getVersion()), value);
     }
     ValidatorService.ValidationResult getValidated(ValKey key);
     default ValidatorService.ValidationResult getValidated(String key) {
          return getValidated(ValKey.of(key, getVersion()));
     }
     Map<ValKey, ValidatorService.ValidationResult> getAllValidated();

     public UcumVersion getVersion();

     void close();
}
