package io.github.fhnaumann;

import au.csiro.ontoserver.OntoOperationPlugin;
import au.csiro.ontoserver.OntoPlugin;
import au.csiro.ontoserver.exceptions.*;
import au.csiro.ontoserver.operations.expand.ExpansionProcessor;
import au.csiro.ontoserver.operations.expand.ExpansionProfile;
import au.csiro.ontoserver.operations.lookup.LookupProcessor;
import au.csiro.ontoserver.operations.lookup.LookupProfile;
import au.csiro.ontoserver.operations.validate.ValidateCodeProfile;
import au.csiro.ontoserver.operations.validate.ValidateProcessor;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.operations.ucum.UCUMExpandOperation;
import io.github.fhnaumann.operations.ucum.UCUMLookupOperation;
import io.github.fhnaumann.operations.ucum.UCUMValidateCodeOperation;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Felix Naumann
 */
@OntoPlugin(name = "UCUMPlugin", description = "Covers the full UCUM syntax.", supportedCodeSystemURL = "http://unitsofmeasure.org")
public class UCUMOntoOperationPlugin implements OntoOperationPlugin {

    private static final Logger log = LoggerFactory.getLogger(UCUMOntoOperationPlugin.class);
    public static String UCUM_SYSTEM = "http://unitsofmeasure.org";
    public static Predicate<ValueSet.ConceptSetComponent> IS_UCUM_SYSTEM = conceptSetComponent -> conceptSetComponent.hasSystem() && UCUM_SYSTEM.equals(conceptSetComponent.getSystem());
    public static Predicate<ValueSet.ValueSetExpansionContainsComponent> IS_UCUM_SYSTEM_ = conceptSetComponent -> conceptSetComponent.hasSystem() && UCUM_SYSTEM.equals(conceptSetComponent.getSystem());
    public static final Set<String> KNOWN_FILTERS = Set.of("canonical", "property");

    private static UCUMOntoOperationPlugin instance;

    private UCUMService service;
    private UCUMValidateCodeOperation validateCodeOperation;
    private UCUMExpandOperation expandOperation;
    private UCUMLookupOperation lookupCodeOperation;
    private UCUMVersionResolver ucumVersionResolver;

    @Override
    public void initialize() {
        service = new UCUMService();

        this.lookupCodeOperation = new UCUMLookupOperation(this, service);
        this.expandOperation = new UCUMExpandOperation(this, service);
        this.validateCodeOperation = new UCUMValidateCodeOperation(this, service, expandOperation);
        this.ucumVersionResolver = new UCUMVersionResolver();

        // register infispan
        //PersistenceRegistry.register("infispan", new InfinispanPersistenceProvider());

        // load code system supplements
        instance = this;
    }

    @Override
    public String name() {
        return "UCUMPlugin";
    }

    @Override
    public String supportedCodeSystemURL() {
        return "http://unitsofmeasure.org";
    }

    @Override
    public void expand(ValueSet valueSet, ExpansionProfile expansionProfile, ExpansionProcessor expansionProcessor) throws PluginBaseException {
        expandOperation.expand(valueSet, expansionProfile, expansionProcessor);
    }

    @Override
    public void validateCode(ValueSet valueSet, Coding coding, ExpansionProfile expansionProfile, ValidateCodeProfile validateCodeProfile, ValidateProcessor validateProcessor) throws PluginBaseException {
        validateCodeOperation.validateCode(valueSet, coding, expansionProfile, validateCodeProfile, validateProcessor);
    }

    public static UCUMOntoOperationPlugin getInstance() {
        return instance;
    }

    @Override
    public void lookup(Coding coding, LookupProfile lookupProfile, LookupProcessor lookupProcessor) throws PluginBaseException {
        lookupCodeOperation.lookup(coding, lookupProfile, lookupProcessor);
    }

    @Override
    public String resolveLatestVersionForCodeSystem(String codeSystemUri, String codeSystemVersion, boolean safeOnly) {
        return ucumVersionResolver.resolveLatestVersionForCodeSystem(codeSystemUri, codeSystemVersion, safeOnly);
    }
}
