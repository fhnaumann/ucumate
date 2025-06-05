package me.fhnau.org.persistence;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.model.UCUMExpression;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import me.fhnau.org.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Felix Naumann
 */
public class PersistenceRegistry implements PersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(PersistenceRegistry.class);

    private static final PersistenceRegistry INSTANCE = new PersistenceRegistry();

    private PersistenceRegistry() {}

    static {
        try {
            Class.forName("me.fhnau.org.persistence.SQLiteAutoRegistrar");
        } catch (ClassNotFoundException ignored) {
            // Persistence module not on classpath — ignore
        }
    }


    private static InMemoryPersistenceProvider cache = new InMemoryPersistenceProvider();
    private static final Map<String, PersistenceProvider> additionalProviders = new HashMap<>();

    public static void initCache() {
        initCache(new Properties());
    }

    public static void initCache(Properties properties) {
        try {
            boolean enableCache = (boolean) properties.getOrDefault("ucumate.cache.enable", true);
            int maxCanonSize = (int) properties.getOrDefault("ucumate.cache.maxCanonSize", 10_000);
            int maxValSize = (int) properties.getOrDefault("ucumate.cache.maxValSize", 10_000);
            boolean recordStats = (boolean) properties.getOrDefault("ucumate.cache.recordStats", false);
            boolean preHeat = (boolean) properties.getOrDefault("ucumate.cache.preheat", false);
            boolean overrideInsteadOfAdd = (boolean) properties.getOrDefault("ucumate.cache.preheat.override", false);
            List<String> defaultPreHeatCodes = PropertiesUtil.readCodeFile(Path.of(PersistenceRegistry.class.getResource("/pre_heat_codes.json").toURI()));
            String preHeatCodesFilename = (String) properties.getOrDefault("ucumate.cache.preheat.codes", "");
            List<String> preHeatCodes = !preHeatCodesFilename.isBlank() ? PropertiesUtil.readCodeFile(Path.of(preHeatCodesFilename)) : List.of();
            if(cache != null) {
                logger.warn("Overriding existing cache.");
                cache.clearCache();
                cache.close();
            }
            if(!enableCache) {
                cache = null;
                return;
            }
            cache = new InMemoryPersistenceProvider(maxCanonSize, maxValSize, recordStats);
            cache.setEnabled(true);
            if(preHeat) {
                List<String> mergedCodes = Stream.concat(overrideInsteadOfAdd ? new ArrayList<String>().stream() : defaultPreHeatCodes.stream(), preHeatCodes.stream())
                        .distinct()
                        .toList();
                cache.preHeat(mergedCodes);
            }
        } catch (IOException | URISyntaxException | ClassCastException e) {
            throw new RuntimeException(e);
        }

    }

    public static void register(String name, PersistenceProvider provider) {
        if(!"sqlite".equals(name)) {
            additionalProviders.remove("sqlite");
        }
        additionalProviders.put(name, provider);
    }

    public static boolean hasAny() {
        return !additionalProviders.isEmpty();
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
    public void close() {
        additionalProviders.forEach((s, persistenceProvider) -> persistenceProvider.close());
        additionalProviders.clear();
    }
}
