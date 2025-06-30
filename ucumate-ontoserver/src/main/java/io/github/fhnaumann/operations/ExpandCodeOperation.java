package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.ValueSet;

import java.util.List;

/**
 * @author Felix Naumann
 */
public interface ExpandCodeOperation {

    public ExpandCodeResult expand(ValueSet valueSet, String textFilter);

    public interface ExpandCodeResult {
        boolean valid();
    }

    public interface Success extends ExpandCodeResult {
        default boolean valid() {
            return true;
        }
        ValueSet valueSet();
    }

    public record PerfectSuccess(ValueSet valueSet) implements Success {}
    public record SuccessWithWarning(ValueSet valueSet, List<String> warnings) implements Success {}

    public record Failure(String failure) implements ExpandCodeResult {
        @Override
        public boolean valid() {
            return false;
        }
    }
}
