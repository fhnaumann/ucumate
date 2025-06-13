package io.github.fhnaumann.configuration;

import java.util.Properties;

public class Configuration {

    private final boolean enablePrefixOnNonMetricUnits;
    private final boolean enableMolMassConversion;
    private final boolean allowAnnotAfterParens;

    private Configuration(boolean enablePrefixOnNonMetricUnits, boolean enableMolMassConversion, boolean allowAnnotAfterParens) {
        this.enablePrefixOnNonMetricUnits = enablePrefixOnNonMetricUnits;
        this.enableMolMassConversion = enableMolMassConversion;
        this.allowAnnotAfterParens = allowAnnotAfterParens;
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

    public static Builder builder() {
        return new Builder();
    }

    public static Configuration fromProps(Properties properties) {
        return new Configuration(
                (boolean) properties.getOrDefault("ucumate.enablePrefixOnNonMetricUnits", true),
                (boolean) properties.getOrDefault("ucumate.enableMolMassConversion", true),
                (boolean) properties.getOrDefault("ucumate.allowAnnotAfterParens", true)
        );
    }

    public static class Builder {
        private boolean enablePrefixOnNonMetricUnits = true;
        private boolean enableMolMassConversion = true;
        private boolean allowAnnotAfterParens = true;

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

        public Configuration build() {
            return new Configuration(enablePrefixOnNonMetricUnits, enableMolMassConversion, allowAnnotAfterParens);
        }
    }
}
