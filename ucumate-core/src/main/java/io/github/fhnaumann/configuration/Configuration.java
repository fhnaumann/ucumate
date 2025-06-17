package io.github.fhnaumann.configuration;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Configuration {

    private final boolean enablePrefixOnNonMetricUnits;
    private final boolean enableMolMassConversion;
    private final boolean allowAnnotAfterParens;
    private final boolean enableSQLitePersistence;
    private final String sqliteDBPath;

    private Configuration(boolean enablePrefixOnNonMetricUnits, boolean enableMolMassConversion, boolean allowAnnotAfterParens, boolean enableSQLitePersistence, String sqliteDBPath) {
        this.enablePrefixOnNonMetricUnits = enablePrefixOnNonMetricUnits;
        this.enableMolMassConversion = enableMolMassConversion;
        this.allowAnnotAfterParens = allowAnnotAfterParens;
        this.enableSQLitePersistence = enableSQLitePersistence;
        this.sqliteDBPath = sqliteDBPath;
    }

    public FeatureFlags asFeatureFlags() {
        return ConfigurationRegistry.getFeatureFlags(this);
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
                "enablePrefixOnNonMetricUnits=" + enablePrefixOnNonMetricUnits +
                ", enableMolMassConversion=" + enableMolMassConversion +
                ", allowAnnotAfterParens=" + allowAnnotAfterParens +
                ", enableSQLitePersistence=" + enableSQLitePersistence +
                ", sqliteDBPath='" + sqliteDBPath + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Configuration fromProps(Properties properties) {
        Properties mergeWithSystemProps = mergeWithSystemProps(properties);
        Properties interpolatedProps = interpolateProps(mergeWithSystemProps);
        return new Configuration(
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

    private static Properties interpolateProps(Properties properties) {
        Properties interpolated = new Properties();
        properties.forEach((o, o2) -> {
            interpolated.put(o, o2.toString().replace("${user.dir}", System.getProperty("user.dir")));
        });
        return interpolated;
    }

    public static class Builder {
        private boolean enablePrefixOnNonMetricUnits = true;
        private boolean enableMolMassConversion = true;
        private boolean allowAnnotAfterParens = true;
        private boolean enableSQLitePersistence;
        private String sqliteDBPath;

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
            return new Configuration(enablePrefixOnNonMetricUnits, enableMolMassConversion, allowAnnotAfterParens, enableSQLitePersistence, sqliteDBPath);
        }
    }
}
