package io.github.fhnaumann.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Felix Naumann
 */
public class ConfigurationRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationRegistry.class);
    private static volatile Configuration instance;
    private static final String DEFAULT_CONFIG_FILE = "ucumate.properties";

    public static void initialize(Configuration configuration) {
        instance = configuration;
    }

    public static Configuration get() {
        if(instance == null) {
            synchronized (ConfigurationRegistry.class) {
                if(instance == null) {
                    // use default configuration
                    instance = loadDefault();
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
