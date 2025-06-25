package io.github.fhnaumann.operations;

import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.UriType;

import java.util.Collection;

/**
 * @author Felix Naumann
 */
public interface LookupCodeOperation {

    public LookupCodeResult lookup(Coding coding, Collection<CodeType> properties);

    public default LookupCodeResult lookup(CodeType codeType, Collection<CodeType> properties) {
        return lookup(new Coding(codeType.getSystem(), codeType.getCode(), codeType.getVersion()), properties);
    }

    public record LookupCodeResult(String name, String version, String display,
                               Collection<LookupCodeProperty> properties) {}

    public record LookupCodeProperty(CodeType code, Coding value, String description) {}
}
