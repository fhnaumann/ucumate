package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.operations.LookupCodeOperation;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;

import java.util.Collection;

/**
 * @author Felix Naumann
 */
public class UCUMLookupCodeOperation implements LookupCodeOperation {

    private final UCUMService ucumService = new UCUMService();

    @Override
    public LookupCodeResult lookup(Coding coding, Collection<CodeType> properties) {
        return null; // todo
    }
}
