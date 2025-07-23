package io.github.fhnaumann.configuration;

import io.github.fhnaumann.util.LogUtil;
import io.github.fhnaumann.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Felix Naumann
 */
public class ConfigurationRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationRegistry.class);

    private static volatile Configuration instance;
    private static final String FALLBACK_CONFIG_FILE = "ucumate_fallback.properties";
    private static final String DEV_CONFIG_FILE = "ucumate.properties";

    public static final List<String> SUPPORTED_UCUM_VERSIONS = List.of("2.2", "2.1");

    public static void initialize(Configuration configuration) {
        if(configuration == null) {
            // reset to the default values (ignore all user set values)
            log.warn("Loading default values because ConfigurationRegistry#initialize has been called with null. This should only be used for testing.");
            configuration = Configuration.fromProps(loadFallBackConfig());
        }
        logConfig(configuration);
        if(!SUPPORTED_UCUM_VERSIONS.contains(configuration.getUCUMVersion())) {
            LogUtil.logAndThrow(log, "Unknown UCUM version '{}' encountered! Only {} are supported.", configuration.getUCUMVersion(), SUPPORTED_UCUM_VERSIONS);
        }
        boolean persistenceModuleOnClassPath = ReflectionUtil.isClassPresent("io.github.fhnaumann.providers.SQLitePersistenceProvider");
        if(configuration.isEnableSQLitePersistence() && !persistenceModuleOnClassPath) {
            log.warn("SQLite Persistence enabled but SQLitePersistenceProvider was not found. Did you include the 'ucumate-persistence' module in your pom.xml?");
        }
        if(!configuration.isEnableSQLitePersistence() && persistenceModuleOnClassPath) {
            log.warn("SQLite Persistence disabled but SQLitePersistenceProvider was found. Did you miss to enable SQLite Persistence at 'ucumate.persistence.sqlite.enable'? If you already use a different persistent storage you can safely ignore this message.");
        }

        FeatureFlags flags = getFeatureFlags(configuration);
        FeatureFlagsContext.set(flags);

        instance = configuration;
    }

    private static void logConfig(Configuration configuration) {
        Properties props = configuration.asProps();
        log.debug("Loading configuration with:");
        props.forEach((key, value) -> log.debug("{} = {}", key, value));
    }

    public static FeatureFlags getFeatureFlags(Configuration configuration) {
        Set<FeatureFlags.Flag> featureFlags = new HashSet<>();
        if(configuration.isEnablePrefixOnNonMetricUnits()) {
            featureFlags.add(FeatureFlags.Flag.PREFIX_ON_NON_METRIC);
        }
        if(configuration.isEnableMolMassConversion()) {
            featureFlags.add(FeatureFlags.Flag.MOL_MASS_CONVERSION);
        }
        if(configuration.isAllowAnnotAfterParens()) {
            featureFlags.add(FeatureFlags.Flag.ANNOT_AFTER_PARENS);
        }
        return FeatureFlags.of(featureFlags);
    }

    public static Configuration get() {
        if(instance == null) {
            synchronized (ConfigurationRegistry.class) {
                if(instance == null) {
                    initialize(loadConfig());
                }
            }
        }
        return instance;
    }

    private static Configuration loadConfig() {
        Properties fallbackProps = loadFallBackConfig();
        Properties devProps = loadDevConfig();
        Properties merged = Configuration.merge(devProps, fallbackProps);
        return Configuration.fromProps(merged);
    }

    public static Properties loadDevConfig() {
        Properties props = new Properties();
        try (var stream = ConfigurationRegistry.class.getClassLoader().getResourceAsStream(DEV_CONFIG_FILE)) {
            if (stream != null) {
                props.load(stream);
            }
        } catch (IOException e) {
            log.warn("No configuration file found at {}. Use system properties or the fluent builder to initialize the configuration. Otherwise the default values will be used.", DEV_CONFIG_FILE);
        }
        return props;
    }

    private static Properties loadFallBackConfig() {
        Properties props = new Properties();
        try (var fallBackStream = ConfigurationRegistry.class.getClassLoader().getResourceAsStream(FALLBACK_CONFIG_FILE)) {
            if (fallBackStream != null) {
                props.load(fallBackStream);
                return props;
            } else {
                return LogUtil.logAndThrow(log, "Failed to load fallback props at {}", FALLBACK_CONFIG_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default configuration file", e);
        }
    }
}
