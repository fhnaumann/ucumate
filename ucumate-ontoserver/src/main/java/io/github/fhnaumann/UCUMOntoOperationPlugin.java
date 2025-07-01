package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.operations.ucum.UCUMExpandOperation;
import io.github.fhnaumann.operations.ucum.UCUMLookupCodeOperation;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Collection;

/**
 * @author Felix Naumann
 */
@OntoPlugin(name = "UCUMPlugin", systems = "http://unitsofmeasure.org")
public class UCUMOntoOperationPlugin implements OntoOperationPlugin {

    private UCUMService service;
    private UCUMLookupCodeOperation lookupCodeOperation;

    @Override
    public void initialize() {
        service = new UCUMService();

        this.lookupCodeOperation = new UCUMLookupCodeOperation();

        // register infispan
        //PersistenceRegistry.register("infispan", new InfinispanPersistenceProvider());

        // load code system supplements
    }

    @Override
    public ExpandResult expand(ValueSet valueSet, String textFilter) {
        return new UCUMExpandOperation(service).expand(valueSet, textFilter);
    }

    @Override
    public LookupCodeResult lookup(Coding coding, Collection<CodeType> properties) {
        return lookupCodeOperation.lookup(coding, properties);
    }

    @Override
    public ValidateCodeResult validate(ValueSet valueSet, CodeableConcept codeableConcept) {
        return null;
    }
}
