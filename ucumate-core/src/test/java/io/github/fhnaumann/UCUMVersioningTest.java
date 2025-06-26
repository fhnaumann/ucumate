package io.github.fhnaumann;

import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.model.UcumVersion;
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
        assertThat(new UCUMService().getUCUMVersion()).isEqualTo(UcumVersion.V2_2);
    }

    @Test
    public void test_UCUM_version_2_2_is_loaded() {
        ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("2.2").build());

        UCUMService service = new UCUMService();
        assertThat(service.getUCUMVersion()).isEqualTo(UcumVersion.V2_2);
        assertThat(service.getUCUMRegistry().getUCUMUnit("[NTU]")).isNotEmpty();
        assertThat(service.getUCUMRegistry().getUCUMUnit("[FNU]")).isNotEmpty();
    }

    @Test
    public void test_UCUM_version_2_1_is_loaded() {
        ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("2.1").build());

        UCUMService service = new UCUMService();
        assertThat(service.getUCUMVersion()).isEqualTo(UcumVersion.V2_1);
        assertThat(service.getUCUMRegistry().getUCUMUnit("[NTU]")).isEmpty();
        assertThat(service.getUCUMRegistry().getUCUMUnit("[FNU]")).isEmpty();
    }

    @Test
    public void test_unknown_UCUM_version_throws() {
        assertThatThrownBy(() -> ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion("1.9").build()));
    }
}
