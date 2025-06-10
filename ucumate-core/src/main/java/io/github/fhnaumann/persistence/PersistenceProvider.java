package io.github.fhnaumann.persistence;

import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.Map;

/**
 * @author Felix Naumann
 */
public interface PersistenceProvider {
     void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value);
     Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key);
     Map<UCUMExpression, Canonicalizer.CanonicalStepResult> getAllCanonical();

     void saveValidated(String key, Validator.ValidationResult value);
     Validator.ValidationResult getValidated(String key);
     Map<String, Validator.ValidationResult> getAllValidated();



     void close();
}
