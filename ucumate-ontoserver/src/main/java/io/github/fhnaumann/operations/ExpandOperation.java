package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.List;

/**
 * Contains methods and classes to handle the $expand operation on ValueSets.
 *
 * @author Felix Naumann
 */
public interface ExpandOperation {

    /**
     * Perform expansion on a provided ValueSet.
     * Some assumptions are made on the ValueSet:
     * <lu>
     *     <li>All codes (implicitly through filter and explicitly through concept) in the ValueSet are from the same system.</li>
     *     <li>The ValueSet is <i>normalized</i>. This means there is no reference to another ValueSet. All information is provided in the compose block.</li>
     * </lu>
     * @param valueSet The ValueSet to be expanded. Expected to be normalized and only to contain codes from one system.
     * @param textFilter Additional text filter that can be applied to filter the expanded codes.
     * @return An {@link ExpandResult} which represents a {@link Success} with an expanded ValueSet or a {@link Failure} with an OperationOutcome.
     */
    public ExpandResult expand(ValueSet valueSet, String textFilter);

    /**
     * Represents a result from the expand operation.
     */
    public interface ExpandResult {
        /**
         * Tests if the result is deemed valid (subclass of {@link Success}) or not (subclass of {@link Failure}).
         * @return true if valid, false otherwise.
         */
        boolean valid();
    }
    /**
     * Represents a valid result from the expand operation. It contains the expanded ValueSet.
     */
    public interface Success extends ExpandResult {
        default boolean valid() {
            return true;
        }

        /**
         * Get the expanded ValueSet.
         * @return The expanded ValueSet.
         */
        ValueSet valueSet();
    }
    /**
     * A successful expansion with no warnings.
     * @param valueSet The expanded ValueSet.
     */
    public record PerfectSuccess(ValueSet valueSet) implements Success {}

    /**
     * A successful expansion but some warnings occurred.
     * @param valueSet The expanded ValueSet.
     * @param warnings The warnings generated during the expansion.
     */
    public record SuccessWithWarning(ValueSet valueSet, List<String> warnings) implements Success {}
    /**
     * An unsuccessful expansion with more details about the failed expansion in the OperationOutcome.
     * @param outcome The OperationOutcome containing more information about the failed expansion.
     */
    public record Failure(OperationOutcome outcome) implements ExpandResult {
        @Override
        public boolean valid() {
            return false;
        }
    }
}
