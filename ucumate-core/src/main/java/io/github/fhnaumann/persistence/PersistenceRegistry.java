package io.github.fhnaumann.persistence;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.configuration.*;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.util.PropertiesUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Felix Naumann
 */
public class PersistenceRegistry implements PersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceRegistry.class);

    private static final PersistenceRegistry INSTANCE = new PersistenceRegistry();

    private PersistenceRegistry() {}

    private static final String CACHE_SETTINGS_PROPERTY_FILE_NAME = "ucumate_fallback.properties";

    private static InMemoryPersistenceProvider cache;
    private static final Map<String, PersistenceProvider> additionalProviders = new HashMap<>();

    static {
        initCache(); // initialize cache with default config or from property file on classpath
        searchSPI();
    }

    /**
     * Looks for a property file on the classpath and loads that.
     * If none found, then it loads the default settings.
     * See more <a href="https://fhnaumann.github.io/ucumate/cache/">in the online documentation</a>.
     */
    public static void initCache() {
        Configuration configuration = ConfigurationRegistry.get();
        initCache(CacheConfiguration.fromProps(configuration.asProps()));
    }

    /**
     * Initialize the cache with given properties.
     * See more <a href="https://fhnaumann.github.io/ucumate/cache/">in the online documentation</a>.
     */
    public static void initCache(CacheConfiguration cacheConfiguration) {
        try {
            boolean enableCache = cacheConfiguration.enable();
            int maxCanonSize = cacheConfiguration.maxCanonSize();
            int maxValSize = cacheConfiguration.maxValSize();
            boolean recordStats = cacheConfiguration.recordStats();
            boolean preHeat = cacheConfiguration.preheat();
            boolean overrideInsteadOfAdd = cacheConfiguration.preheatOverride();
            List<String> defaultPreHeatCodes = PropertiesUtil.readCodeFile(PersistenceRegistry.class.getClassLoader().getResourceAsStream("pre_heat_codes.json"));
            String preHeatCodesFilename = cacheConfiguration.preheatCodeFilename();
            List<String> preHeatCodes = !preHeatCodesFilename.isBlank() ? PropertiesUtil.readCodeFile(preHeatCodesFilename) : List.of();
            if(cache != null) {
                logger.warn("Overriding existing cache.");
                cache.clearCache();
                cache.close();
            }
            if(!enableCache && cache != null) {
                    cache.setEnabled(false);
            }

            cache = new InMemoryPersistenceProvider(maxCanonSize, maxValSize, recordStats);
            cache.setEnabled(enableCache);
            if(enableCache && preHeat) {
                List<String> mergedCodes = Stream.concat(overrideInsteadOfAdd ? new ArrayList<String>().stream() : defaultPreHeatCodes.stream(), preHeatCodes.stream())
                        .distinct()
                        .toList();
                cache.preHeat(mergedCodes);
            }
        } catch (IOException | ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Register a new PersistenceProvider. Call this if you want to add your own custom storage.
     * Some common existing PersistenceProviders are provided in the ucumate-persistence module.
     * @param name The unique name of the persistence provider.
     * @param provider The provider instance.
     */
    public static void register(String name, PersistenceProvider provider) {
        if(!"sqlite".equals(name)) {
            logger.debug("Removed SQLite persistence provider because {} is being registered", provider.getClass().getSimpleName());
            additionalProviders.remove("sqlite");
        }
        PersistenceProvider old = additionalProviders.get(name);
        if(old != null) {
            old.close();
        }
        logger.debug("Registering {}", provider.getClass().getSimpleName());
        additionalProviders.put(name, provider);

        // load registry into cache if enabled
        if(cache != null && cache.isEnabled()) {
            logger.debug("Populating cache with values from {}", provider.getClass().getSimpleName());

            copyRegistryIntoNewProvider(provider);

            provider.getAllValidated().forEach(cache::saveValidated);
            provider.getAllCanonical().forEach(cache::saveCanonical);
        }
    }

    private static void copyRegistryIntoNewProvider(PersistenceProvider provider) {
        Arrays.stream(UcumVersion.values())
                        .forEach(ucumVersion -> {
                            VersionSpecificUCUMRegistry registry = UCUMRegistry.getInstance().getVersionSpecificUCUMRegistry(ucumVersion);
                            registry.getUCUMUnits().forEach(unit -> {
                                UCUMExpression.SingleUnitTerm singleUnitTerm;
                                try {
                                    singleUnitTerm = (UCUMExpression.SingleUnitTerm) SoloTermBuilder.builder().withoutPrefix(unit).noExpNoAnnot().asTerm().build();
                                } catch (ClassCastException e) {
                                    throw new RuntimeException("Unexpected error during registry provider transfer: Expected the registry to only contain SingleUnitTerms.", e);
                                }
                                // always "simple success" because expressions in the registry are single terms (and always valid)
                                ValidatorService.ValidationResult valResult = new ValidatorService.SimpleSuccess(singleUnitTerm);
                                provider.saveValidated(ValKey.of(null, ucumVersion), valResult);
                            });
                        });
    }

    public static void unregister(String name) {
        PersistenceProvider removed = additionalProviders.remove(name);
        if(removed != null) {
            removed.close();
            logger.debug("Unregistered {}: {} from the persistence registry.", name, removed.getClass().getSimpleName());
        }
    }

    public static boolean hasAny() {
        return !additionalProviders.isEmpty();
    }

    public static void searchSPI() {
        if(ConfigurationRegistry.get().isEnableSQLitePersistence()) {
            ServiceLoader.load(PersistenceProvider.class).forEach(persistenceProvider -> {
                String name = persistenceProvider.getClass().getSimpleName();
                // only overwrite if not already exists
                if(additionalProviders.get(name) == null) {
                    register(name, persistenceProvider);
                }
            });
        }
    }

    public static void disableInMemoryCache(boolean deleteCacheEntries) {
        if(cache == null) {
            return;
        }
        if(deleteCacheEntries) {
            cache.clearCache();
        }
        cache.setEnabled(false);
    }

    public static PersistenceRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void saveCanonical(CanonKey key, Canonicalizer.CanonicalStepResult value) {
        cache.saveCanonical(key, value);
        additionalProviders.forEach((s, entry) -> entry.saveCanonical(key, value));
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(CanonKey key) {
        Canonicalizer.CanonicalStepResult canonicalStepResult = cache.getCanonical(key);
        if(canonicalStepResult != null) {
            return canonicalStepResult;
        }
        for(Map.Entry<String, PersistenceProvider>  provider : additionalProviders.entrySet()) {
            canonicalStepResult = provider.getValue().getCanonical(key);
            if(canonicalStepResult != null) {
                return canonicalStepResult;
            }
        }
        return null;
    }

    @Override
    public Map<CanonKey, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        if(cache != null && cache.isEnabled()) {
            return cache.getAllCanonical();
        }
        if(additionalProviders.isEmpty()) {
            return Map.of();
        }
        return additionalProviders.entrySet().stream().findFirst().get().getValue().getAllCanonical(); //NOSONAR
    }

    @Override
    public void saveValidated(ValKey key, ValidatorService.ValidationResult value) {
        cache.saveValidated(key, value);
        additionalProviders.forEach((s, entry) -> entry.saveValidated(key, value));
    }

    @Override
    public ValidatorService.ValidationResult getValidated(ValKey key) {
        ValidatorService.ValidationResult validationResult = cache.getValidated(key);
        if(validationResult != null) {
            return validationResult;
        }
        for(Map.Entry<String, PersistenceProvider>  provider : additionalProviders.entrySet()) {
            validationResult = provider.getValue().getValidated(key);
            if(validationResult != null) {
                return validationResult;
            }
        }
        return null;
    }

    @Override
    public Map<ValKey, ValidatorService.ValidationResult> getAllValidated() {
        if(cache != null && cache.isEnabled()) {
            return cache.getAllValidated();
        }
        if(additionalProviders.isEmpty()) {
            return Map.of();
        }
        return additionalProviders.entrySet().stream().findFirst().get().getValue().getAllValidated(); //NOSONAR
    }

    @Override
    public Stream<Map.Entry<ValKey, ValidatorService.ValidationResult>> getAllValidatedLazy() {
        if(cache != null && cache.isEnabled()) {
            return cache.getAllValidatedLazy();
        }
        if(additionalProviders.isEmpty()) {
            return Stream.empty();
        }
        return additionalProviders.entrySet().stream()
                .findFirst()
                .map(entry -> entry.getValue().getAllValidatedLazy())
                .orElse(Stream.empty());
    }

    @Override
    public void close() {
        additionalProviders.forEach((s, persistenceProvider) -> persistenceProvider.close());
        additionalProviders.clear();
    }
}
