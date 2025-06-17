package io.github.fhnaumann.persistence;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.Map;

/**
 * @author Felix Naumann
 */
public interface PersistenceProvider {
     void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value);
     default void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
          saveCanonical(CanonKey.of(key), value);
     }
     Canonicalizer.CanonicalStepResult getCanonical(CanonKey key);
     default Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
          return getCanonical(CanonKey.of(key));
     }
     Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical();

     void saveValidated(ValKey key, Validator.ValidationResult value);
     default void saveValidated(String key, Validator.ValidationResult value) {
          saveValidated(ValKey.of(key), value);
     }
     Validator.ValidationResult getValidated(ValKey key);
     default Validator.ValidationResult getValidated(String key) {
          return getValidated(ValKey.of(key));
     }
     Map<ValKey, Validator.ValidationResult> getAllValidated();



     void close();
}
