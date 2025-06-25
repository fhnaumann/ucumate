package io.github.fhnaumann.configuration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Configuration {

    private final List<String> supportedUCUMVersions;
    private final String defaultUCUMVersion;
    private final Boolean enablePrefixOnNonMetricUnits;
    private final Boolean enableMolMassConversion;
    private final Boolean allowAnnotAfterParens;
    private final Boolean enableSQLitePersistence;
    private final String sqliteDBPath;

    private Configuration(List<String> supportedUCUMVersions, String defaultUCUMVersion, Boolean enablePrefixOnNonMetricUnits, Boolean enableMolMassConversion, Boolean allowAnnotAfterParens, Boolean enableSQLitePersistence, String sqliteDBPath) {
        this.supportedUCUMVersions = supportedUCUMVersions;
        this.defaultUCUMVersion = defaultUCUMVersion;
        this.enablePrefixOnNonMetricUnits = enablePrefixOnNonMetricUnits;
        this.enableMolMassConversion = enableMolMassConversion;
        this.allowAnnotAfterParens = allowAnnotAfterParens;
        this.enableSQLitePersistence = enableSQLitePersistence;
        this.sqliteDBPath = sqliteDBPath;
    }

    public FeatureFlags asFeatureFlags() {
        return ConfigurationRegistry.getFeatureFlags(this);
    }

    public List<String> getSupportedUCUMVersions() {
        return List.copyOf(supportedUCUMVersions);
    }

    public String getDefaultUCUMVersion() {
        return defaultUCUMVersion;
    }

    public boolean isEnablePrefixOnNonMetricUnits() {
        return enablePrefixOnNonMetricUnits;
    }

    public boolean isEnableMolMassConversion() {
        return enableMolMassConversion;
    }

    public boolean isAllowAnnotAfterParens() {
        return allowAnnotAfterParens;
    }

    public boolean isEnableSQLitePersistence() {
        return enableSQLitePersistence;
    }

    public String getSqliteDBPath() {
        return sqliteDBPath;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "supportedUCUMVersions=" + supportedUCUMVersions +
                ", defaultUCUMVersion='" + defaultUCUMVersion + '\'' +
                ", enablePrefixOnNonMetricUnits=" + enablePrefixOnNonMetricUnits +
                ", enableMolMassConversion=" + enableMolMassConversion +
                ", allowAnnotAfterParens=" + allowAnnotAfterParens +
                ", enableSQLitePersistence=" + enableSQLitePersistence +
                ", sqliteDBPath='" + sqliteDBPath + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public Properties asProps() {
        Properties props = new Properties();
        // Some variables may be null if the builder was used to construct them
        if(supportedUCUMVersions != null) {
            props.put("ucumate.ucumVersion.supported", String.join(",", supportedUCUMVersions));
        }
        if(defaultUCUMVersion != null) {
            props.put("ucumate.ucumVersion.default", defaultUCUMVersion);
        }
        if(enablePrefixOnNonMetricUnits != null) {
            props.put("ucumate.enablePrefixOnNonMetricUnits", enablePrefixOnNonMetricUnits);
        }
        if(enableMolMassConversion != null) {
            props.put("ucumate.enableMolMassConversion", enableMolMassConversion);
        }
        if(allowAnnotAfterParens != null) {
            props.put("ucumate.allowAnnotAfterParens", allowAnnotAfterParens);
        }
        if(enableSQLitePersistence != null) {
            props.put("ucumate.persistence.sqlite.enable", enableSQLitePersistence);
        }
        if(sqliteDBPath != null) {
            props.put("ucumate.persistence.sqlite.dbpath", sqliteDBPath);
        }
        return props;
    }

    public static Configuration fromProps(Properties properties) {
        Properties mergeWithSystemProps = mergeWithSystemProps(properties);
        Properties interpolatedProps = interpolateProps(mergeWithSystemProps);
        return new Configuration(
                Arrays.asList(interpolatedProps.getProperty("ucumate.ucumVersion.supported").split(",")),
                interpolatedProps.getProperty("ucumate.ucumVersion.default"),
                Boolean.parseBoolean(interpolatedProps.getProperty("ucumate.enablePrefixOnNonMetricUnits")),
                Boolean.parseBoolean(interpolatedProps.getProperty("ucumate.enableMolMassConversion")),
                Boolean.parseBoolean(interpolatedProps.getProperty("ucumate.allowAnnotAfterParens")),
                Boolean.parseBoolean(interpolatedProps.getProperty("ucumate.persistence.sqlite.enable")),
                interpolatedProps.getProperty("ucumate.persistence.sqlite.dbpath")
                );
    }

    private static Properties mergeWithSystemProps(Properties properties) {
        Properties resolved = new Properties();
        for (String key : properties.stringPropertyNames()) {
            String sysProp = System.getProperty(key);
            String value = (sysProp != null) ? sysProp : properties.getProperty(key);
            resolved.setProperty(key, value);
        }
        return resolved;
    }

    public static Properties merge(Properties basis, Properties useIfMissing) {
        Properties merged = new Properties();
        for (String key : useIfMissing.stringPropertyNames()) {
            merged.setProperty(key, useIfMissing.getProperty(key));
        }
        for (String key : basis.stringPropertyNames()) {
            merged.setProperty(key, basis.getProperty(key));
        }
        return merged;
    }

    private static Properties interpolateProps(Properties properties) {
        Properties interpolated = new Properties();
        properties.forEach((o, o2) -> {
            interpolated.put(o, o2.toString().replace("${user.dir}", System.getProperty("user.dir")));
        });
        return interpolated;
    }

    public static class Builder {
        private List<String> supportedUCUMVersions;
        private String defaultUCUMVersion;
        private Boolean enablePrefixOnNonMetricUnits;
        private Boolean enableMolMassConversion = true;
        private Boolean allowAnnotAfterParens = true;
        private Boolean enableSQLitePersistence;
        private String sqliteDBPath;

        public Builder withSupportedUCUMVersions(Collection<String> supportedUCUMVersions) {
            this.supportedUCUMVersions = new ArrayList<>(supportedUCUMVersions);
            return this;
        }

        public Builder withDefaultUCUMVersion(String defaultUCUMVersion) {
            this.defaultUCUMVersion = defaultUCUMVersion;
            return this;
        }

        public Builder enablePrefixOnNonMetricUnits(boolean value) {
            this.enablePrefixOnNonMetricUnits = value;
            return this;
        }

        public Builder enableMolMassConversion(boolean value) {
            this.enableMolMassConversion = value;
            return this;
        }

        public Builder allowAnnotAfterParens(boolean value) {
            this.allowAnnotAfterParens = value;
            return this;
        }

        public Builder enableSQLitePersistence(boolean value) {
            this.enableSQLitePersistence = value;
            return this;
        }

        public Builder sqliteDBPath(String value) {
            this.sqliteDBPath = value;
            return this;
        }

        public Configuration build() {
            /*
            At the time that this method is called, there will always be another configuration already loaded.
            This can either be a dev provided ucumate.properties file or the default ucumate_fallback.properties file.
            The fluent builder should override fields where they have been explicitly provided. Otherwise, use whatever
            value already exists. Therefore, it's necessary to merge the props here.
             */
            Configuration oldConfiguration = ConfigurationRegistry.get();
            Configuration newConfiguration = new Configuration(supportedUCUMVersions, defaultUCUMVersion, enablePrefixOnNonMetricUnits, enableMolMassConversion, allowAnnotAfterParens, enableSQLitePersistence, sqliteDBPath);
            Properties merged = merge(newConfiguration.asProps(), oldConfiguration.asProps());
            return Configuration.fromProps(merged);
        }
    }
}
