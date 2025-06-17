package io.github.fhnaumann.configuration;

import io.github.fhnaumann.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Felix Naumann
 */
public class ConfigurationRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationRegistry.class);
    private static volatile Configuration instance;
    private static final String DEFAULT_CONFIG_FILE = "ucumate.properties";

    public static void initialize(Configuration configuration) {
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
                    // use default configuration
                    initialize(loadDefault());
                }
            }
        }
        return instance;
    }

    private static Configuration loadDefault() {
        Properties props = new Properties();
        try (var stream = ConfigurationRegistry.class.getClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (stream != null) {
                props.load(stream);
                return Configuration.fromProps(props);
            } else {
                log.error("Failed to load default configuration props!");
                return Configuration.fromProps(new Properties());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default configuration file", e);
        }
    }
}
