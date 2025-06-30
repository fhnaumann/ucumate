package io.github.fhnaumann;

import io.github.fhnaumann.operations.ExpandCodeOperation;
import io.github.fhnaumann.operations.LookupCodeOperation;
import io.github.fhnaumann.operations.ValidateCodeOperation;
import io.github.fhnaumann.operations.ucum.UCUMLookupCodeOperation;
import io.github.fhnaumann.persistence.PersistenceRegistry;
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

    private UCUMLookupCodeOperation lookupCodeOperation;

    @Override
    public void initialize() {
        this.lookupCodeOperation = new UCUMLookupCodeOperation();

        // register infispan
        //PersistenceRegistry.register("infispan", new InfinispanPersistenceProvider());

        // load code system supplements
    }

    @Override
    public ExpandCodeResult expand(ValueSet valueSet, String textFilter) {
        return null;
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
