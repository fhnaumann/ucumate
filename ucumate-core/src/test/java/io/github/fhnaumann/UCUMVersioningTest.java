package io.github.fhnaumann;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.util.UCUMRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Felix Naumann
 */
public class UCUMVersioningTest {

    @Test
    public void test_default_UCUM_version_is_2_2() {
        ConfigurationRegistry.initialize(null);
        assertThat(UCUMRegistry.getInstance().getLoadedUCUMEssenceVersion()).isEqualTo("2.2");
    }

    @Test
    public void test_UCUM_version_2_2_is_loaded() {
        ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("2.2").build());

        assertThat(UCUMRegistry.getInstance().getLoadedUCUMEssenceVersion()).isEqualTo("2.2");
        assertThat(UCUMRegistry.getInstance().getUCUMUnit("[NTU]")).isNotEmpty();
        assertThat(UCUMRegistry.getInstance().getUCUMUnit("[FNU]")).isNotEmpty();
    }

    @Test
    public void test_UCUM_version_2_1_is_loaded() {
        ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("2.1").build());

        assertThat(UCUMRegistry.getInstance().getLoadedUCUMEssenceVersion()).isEqualTo("2.1");
        assertThat(UCUMRegistry.getInstance().getUCUMUnit("[NTU]")).isEmpty();
        assertThat(UCUMRegistry.getInstance().getUCUMUnit("[FNU]")).isEmpty();
    }

    @Test
    public void test_unknown_UCUM_version_throws() {
        assertThatThrownBy(() -> ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("1.9").build()));
    }
}
