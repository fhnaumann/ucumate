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
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.PropertiesUtil;
import org.hl7.fhir.r4.model.*;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Felix Naumann
 */
@OntoPlugin(name = "UCUMPlugin", description = "Covers the full UCUM syntax.", supportedCodeSystemURL = "http://unitsofmeasure.org")
@Service
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

    private final DefaultCacheManager defaultCacheManager;
    private InfinispanPersistenceProvider infinispanPersistenceProvider;

    @Autowired
    public UCUMOntoOperationPlugin(DefaultCacheManager cacheManager) {
        this.defaultCacheManager = cacheManager;
    }

    @Override
    public void initialize() {
        instance = this;

        // create UCUM support services
        service = new UCUMService();
        this.lookupCodeOperation = new UCUMLookupOperation(this, service);
        this.expandOperation = new UCUMExpandOperation(this, service);
        this.validateCodeOperation = new UCUMValidateCodeOperation(this, service, expandOperation);
        this.ucumVersionResolver = new UCUMVersionResolver();

        // register infinispan
        if(infinispanPersistenceProvider == null) {
            this.infinispanPersistenceProvider = new InfinispanPersistenceProvider(defaultCacheManager);
            PersistenceRegistry.register("infinispan-cache", infinispanPersistenceProvider);
            String preheatCodeFilename = "pre_heat_codes.json";
            try {
                List<String> defaultPreHeatCodes = PropertiesUtil.readCodeFile(PersistenceRegistry.class.getClassLoader().getResourceAsStream(preheatCodeFilename));
                log.debug("Preheating Infinispan Persistence Provider (de-facto cache) with codes from {}.", preheatCodeFilename);
                //this.infinispanPersistenceProvider.preheat(defaultPreHeatCodes);
            } catch (IOException e) {
                throw new RuntimeException("Failed preheat codes from '%s'".formatted(preheatCodeFilename), e);
            }
        }
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

    @Override
    public void lookup(Coding coding, LookupProfile lookupProfile, LookupProcessor lookupProcessor) throws PluginBaseException {
        lookupCodeOperation.lookup(coding, lookupProfile, lookupProcessor);
    }

    @Override
    public String resolveLatestVersionForCodeSystem(String codeSystemUri, String codeSystemVersion, boolean safeOnly) {
        return ucumVersionResolver.resolveLatestVersionForCodeSystem(codeSystemUri, codeSystemVersion, safeOnly);
    }

    public static UCUMOntoOperationPlugin getInstance() {
        return instance;
    }
}
