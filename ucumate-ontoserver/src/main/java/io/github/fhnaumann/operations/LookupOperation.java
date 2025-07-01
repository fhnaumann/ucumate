package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import java.util.Collection;
import java.util.Map;

/**
 * Contains methods and classes to handle the $lookup operation on CodeSystems.
 *
 * @author Felix Naumann
 */
public interface LookupOperation {

    /**
     * Perform lookup on a provided Coding.
     * Requires the version to be set.
     * <br>
     * Subclasses define which additional properties to support.
     *
     * @param coding The coding with a specified version.
     * @param properties Additional properties of the provided code to be returned.
     * @return A {@link LookupResult} which represents a {@link Success} with information about the code or a {@link Failure} with an OperationOutcome.
     */
    public LookupResult lookup(Coding coding, Collection<String> properties);

    public default LookupResult lookup(CodeType codeType, Collection<String> properties) {
        return lookup(new Coding(codeType.getSystem(), codeType.getCode(), codeType.getVersion()), properties);
    }

    /**
     * Represents a result from the lookup operation.
     */
    public sealed interface LookupResult {}

    /**
     * Represents a successful lookup operation. It contains information about the code that was looked up.
     * @param code The original code that was provided.
     * @param name The display name of the system.
     * @param version The version of the system.
     * @param display The display string of the code.
     * @param returnedProperties A map containing the provided properties as keys and their determined values.
     */
    public record Success(String code, String name, String version, String display, Map<String, String> returnedProperties) implements LookupResult {}

    /**
     * Represents an unsuccessful lookup with more details about the failed lookup in the OperationOutcome.
     * @param operationOutcome The OperationOutcome containing more information about the failed expansion.
     */
    public record Failure(OperationOutcome operationOutcome) implements LookupResult {}
}
