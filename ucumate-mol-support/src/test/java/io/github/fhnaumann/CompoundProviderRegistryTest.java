package io.github.fhnaumann;

import io.github.fhnaumann.compounds.CompoundProvider;
import io.github.fhnaumann.compounds.CompoundProviderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class CompoundProviderRegistryTest {

    @BeforeEach
    public void setUp() {
        //CompoundProviderRegistry.resetToDefault();
    }

    @Test
    public void test_custom_compound_provider_can_be_registered() {
        CompoundProviderRegistry.registerCustom(new CompoundProviderStub());
        assertThat(CompoundProviderRegistry.get())
                .isNotNull()
                .isExactlyInstanceOf(CompoundProviderStub.class);
    }

    private static class CompoundProviderStub implements CompoundProvider {

        @Override
        public String findByName(String name) {
            return "";
        }

        @Override
        public String findBySynonym(String synonym) {
            return "";
        }

        @Override
        public String findByFormular(String formular) {
            return "";
        }

        @Override
        public String findByCasRn(String casRn) {
            return "";
        }

        @Override
        public String findByInchiKey(String inchiKey) {
            return "";
        }

        @Override
        public String findByMatch(String value) {
            return "";
        }
    }
}
