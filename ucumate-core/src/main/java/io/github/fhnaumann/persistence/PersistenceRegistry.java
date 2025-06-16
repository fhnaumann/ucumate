package io.github.fhnaumann.persistence;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMExpression;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import io.github.fhnaumann.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Felix Naumann
 */
public class PersistenceRegistry implements PersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceRegistry.class);

    private static final PersistenceRegistry INSTANCE = new PersistenceRegistry();

    private PersistenceRegistry() {}

    private static final String CACHE_SETTINGS_PROPERTY_FILE_NAME = "ucumate.properties";

    public static InMemoryPersistenceProvider cache;
    private static final Map<String, PersistenceProvider> additionalProviders = new HashMap<>();

    static {
        //try {
            initCache(); // initialize cache with default config or from property file on classpath
            // Class.forName("io.github.fhnaumann.SQLiteAutoRegistrar"); // try to auto-register sqlite provider if persistence module is on classpath, otherwise ignore
            searchSPI();
        //} catch (ClassNotFoundException ignored) {
            // Persistence module not on classpath — ignore
        //}
    }

    /**
     * Looks for a property file on the classpath and loads that.
     * If none found, then it loads the default settings.
     * See more <a href="https://fhnaumann.github.io/ucumate/cache/">in the online documentation</a>.
     */
    public static void initCache() {
        Properties props = findCacheSettingsFromPropertyFileOnClasspath();
        initCache(props);
    }

    /**
     * Initialize the cache with given properties.
     * See more <a href="https://fhnaumann.github.io/ucumate/cache/">in the online documentation</a>.
     * @param properties The properties for the cache settings.
     */
    public static void initCache(Properties properties) {
        try {
            boolean enableCache = (boolean) properties.getOrDefault("ucumate.cache.enable", true);
            int maxCanonSize = (int) properties.getOrDefault("ucumate.cache.maxCanonSize", 10_000);
            int maxValSize = (int) properties.getOrDefault("ucumate.cache.maxValSize", 10_000);
            boolean recordStats = (boolean) properties.getOrDefault("ucumate.cache.recordStats", false);
            boolean preHeat = (boolean) properties.getOrDefault("ucumate.cache.preheat", false);
            boolean overrideInsteadOfAdd = (boolean) properties.getOrDefault("ucumate.cache.preheat.override", false);
            List<String> defaultPreHeatCodes = PropertiesUtil.readCodeFile(PersistenceRegistry.class.getClassLoader().getResourceAsStream("pre_heat_codes.json"));
            String preHeatCodesFilename = (String) properties.getOrDefault("ucumate.cache.preheat.codes", "");
            List<String> preHeatCodes = !preHeatCodesFilename.isBlank() ? PropertiesUtil.readCodeFile(preHeatCodesFilename) : List.of();
            if(cache != null) {
                logger.warn("Overriding existing cache.");
                cache.clearCache();
                cache.close();
            }
            if(!enableCache) {
                if(cache != null) {
                    cache.setEnabled(false);
                }
            }
            cache = new InMemoryPersistenceProvider(maxCanonSize, maxValSize, recordStats);
            cache.setEnabled(true);
            if(preHeat) {
                List<String> mergedCodes = Stream.concat(overrideInsteadOfAdd ? new ArrayList<String>().stream() : defaultPreHeatCodes.stream(), preHeatCodes.stream())
                        .distinct()
                        .toList();
                cache.preHeat(mergedCodes);
            }
        } catch (IOException | ClassCastException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties findCacheSettingsFromPropertyFileOnClasspath() {
        Properties props = new Properties();
        try (InputStream in = PersistenceRegistry.class.getClassLoader().getResourceAsStream(CACHE_SETTINGS_PROPERTY_FILE_NAME)) {
            if (in != null) {
                props.load(in);
                logger.debug("Loaded properties from {}.", CACHE_SETTINGS_PROPERTY_FILE_NAME);
                logger.debug("Picked up: {}", props);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load ucumate.properties", e);
        }
        return props;
    }

    /**
     * Register a new PersistenceProvider. Call this if you want to add your own custom storage.
     * Some common existing PersistenceProviders are provided in the ucumate-persistence module.
     * @param name The unique name of the persistence provider.
     * @param provider The provider instance.
     */
    public static void register(String name, PersistenceProvider provider) {
        if(!"sqlite".equals(name)) {
            additionalProviders.remove("sqlite");
        }
        PersistenceProvider old = additionalProviders.get(name);
        if(old != null) {
            old.close();
        }
        additionalProviders.put(name, provider);

        // try and load saved data into cache if enabled
        if(cache != null && cache.isEnabled()) {
            provider.getAllValidated().forEach(cache::saveValidated);
            provider.getAllCanonical().forEach(cache::saveCanonical);
        }
    }

    public static boolean hasAny() {
        return !additionalProviders.isEmpty();
    }

    public static void searchSPI() {
        if(ConfigurationRegistry.get().isEnableSQLitePersistence()) {
            ServiceLoader.load(PersistenceProvider.class).forEach(persistenceProvider -> {
                String name = persistenceProvider.getClass().getSimpleName();
                // don't overwrite if already exists
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
    public void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
        cache.saveCanonical(key, value);
        additionalProviders.forEach((s, entry) -> entry.saveCanonical(key, value));
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
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
    public Map<UCUMExpression, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        if(cache != null && cache.isEnabled()) {
            return cache.getAllCanonical();
        }
        if(additionalProviders.isEmpty()) {
            return Map.of();
        }
        return additionalProviders.entrySet().stream().findFirst().get().getValue().getAllCanonical();
    }

    @Override
    public void saveValidated(String key, Validator.ValidationResult value) {
        cache.saveValidated(key, value);
        additionalProviders.forEach((s, entry) -> entry.saveValidated(key, value));
    }

    @Override
    public Validator.ValidationResult getValidated(String key) {
        Validator.ValidationResult validationResult = cache.getValidated(key);
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
    public Map<String, Validator.ValidationResult> getAllValidated() {
        if(cache != null && cache.isEnabled()) {
            return cache.getAllValidated();
        }
        if(additionalProviders.isEmpty()) {
            return Map.of();
        }
        return additionalProviders.entrySet().stream().findFirst().get().getValue().getAllValidated();
    }

    @Override
    public void close() {
        additionalProviders.forEach((s, persistenceProvider) -> persistenceProvider.close());
        additionalProviders.clear();
    }
}
