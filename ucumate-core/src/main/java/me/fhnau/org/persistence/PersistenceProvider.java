package me.fhnau.org.persistence;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.model.UCUMExpression;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
