package io.github.fhnaumann;

import io.github.fhnaumann.compounds.CompoundProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class NistCompoundProviderSPITest {

    @Test
    void test_spi_provider_is_auto_discovered() {
        ServiceLoader<CompoundProvider> loader = ServiceLoader.load(CompoundProvider.class);
        List<CompoundProvider> providers = StreamSupport.stream(loader.spliterator(), false).toList();

        assertThat(providers).isNotEmpty();
        assertThat(providers.get(0).getClass().getName()).contains("NistCompoundProvider");

        String mw = providers.get(0).findByCasRn("1309-37-1"); // iron oxide
        assertThat(mw).isEqualTo("159.688"); // whatever it should return
    }
}
