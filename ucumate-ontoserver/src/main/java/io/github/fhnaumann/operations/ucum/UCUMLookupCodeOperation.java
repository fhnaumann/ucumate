package io.github.fhnaumann.operations.ucum;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.operations.LookupCodeOperation;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;

import java.util.Collection;
import java.util.Set;

/**
 * @author Felix Naumann
 */
public class UCUMLookupCodeOperation implements LookupCodeOperation {

    private final Set<String> REQUESTABLE_PROPS = Set.of(
            "url", "name", "version", "display", "definition", "designation", "parent", "child", // FHIR defaults
            "codeCaseInsensitive", "unitName", "property", "metric", "special", "class", "dimensionality", "canonical", "commonSyntax", "latexSyntax"
    );

    private final UCUMService ucumService = new UCUMService();

    @Override
    public LookupCodeResult lookup(Coding coding, Collection<CodeType> properties) {

    }


}
