package me.fhnau.org.configuration;

import java.util.Properties;

public class Configuration {

    private boolean enablePersistence = false;
    private Properties jpaProperties;

    public boolean isPersistenceEnabled() {
        return enablePersistence;
    }

    public void setEnablePersistence(boolean enablePersistence) {
        this.enablePersistence = enablePersistence;
    }

    public Properties getJpaProperties() {
        return jpaProperties;
    }

    public void setJpaProperties(Properties jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    public static Configuration defaultConfig() {
        return new Configuration();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Properties props = new Properties();
        private boolean persistence;

        public Builder enablePersistence(boolean enabled) {
            this.persistence = enabled;
            return this;
        }

        public Builder dbProperty(String key, String value) {
            props.setProperty(key, value);
            return this;
        }

        public Configuration build() {
            Configuration config = new Configuration();
            config.enablePersistence = persistence;
            config.jpaProperties = props;
            return config;
        }
    }
}
