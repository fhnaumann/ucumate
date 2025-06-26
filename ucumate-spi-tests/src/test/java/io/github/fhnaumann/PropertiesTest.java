package io.github.fhnaumann;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class PropertiesTest {

    private static Configuration config;

    @BeforeAll
    public static void init() {
        config = ConfigurationRegistry.get(); // trigger initialization
    }

    @Test
    public void test_properties_file_is_loaded_and_preferred() {
        assertThat(config.getUCUMVersion()).isEqualTo("2.1");
    }

    @Test
    public void test_fallback_property_is_used_when_missing_in_provided_properties() {
        // The src/test/resources/ucumate.properties does not contain "ucumate.enablePrefixOnNonMetricUnits"
        assertThat(config.isEnablePrefixOnNonMetricUnits()).isEqualTo(true);
    }
}
