package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.operations.ucum.UCUMExpandOperation;
import io.github.fhnaumann.operations.ucum.UCUMLookupOperation;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Collection;

/**
 * @author Felix Naumann
 */
@OntoPlugin(name = "UCUMPlugin", systems = "http://unitsofmeasure.org")
public class UCUMOntoOperationPlugin implements OntoOperationPlugin {

    public static String UCUM_SYSTEM = "http://unitsofmeasure.org";

    private UCUMService service;
    private UCUMLookupOperation lookupCodeOperation;

    @Override
    public void initialize() {
        service = new UCUMService();

        this.lookupCodeOperation = new UCUMLookupOperation(service);

        // register infispan
        //PersistenceRegistry.register("infispan", new InfinispanPersistenceProvider());

        // load code system supplements
    }

    @Override
    public ExpandResult expand(ValueSet valueSet, String textFilter) {
        return new UCUMExpandOperation(service).expand(valueSet, textFilter);
    }

    @Override
    public LookupResult lookup(Coding coding, Collection<String> properties) {
        return lookupCodeOperation.lookup(coding, properties);
    }

    @Override
    public ValidateCodeResult validate(CodeSystem codeSystem, CodeableConcept codeableConcept) {
        return null;
    }

    @Override
    public ValidateCodeResult validate(ValueSet valueSet, CodeableConcept codeableConcept) {
        return null;
    }
}
