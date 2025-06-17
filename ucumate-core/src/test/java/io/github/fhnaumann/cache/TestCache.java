package io.github.fhnaumann.cache;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class TestCache {

    private static final Configuration ALL_ENABLED = Configuration.builder()
            .enableMolMassConversion(true)
            .enablePrefixOnNonMetricUnits(true)
            .allowAnnotAfterParens(true)
            .build();

    private static final Configuration DISABLE_PREFIX_ON_NON_METRIC = Configuration.builder()
            .enableMolMassConversion(true)
            .enablePrefixOnNonMetricUnits(false)
            .allowAnnotAfterParens(true)
            .build();

    private static final Configuration DISABLE_MOL_MASS_CONVERSION = Configuration.builder()
            .enableMolMassConversion(false)
            .enablePrefixOnNonMetricUnits(true)
            .allowAnnotAfterParens(true)
            .build();

    @BeforeAll
    public static void init() {
        Properties props = new Properties();
        props.setProperty("ucumate.cache.enable", "true");
        PersistenceRegistry.initCache(props);
    }

    @Test
    public void val_key_is_unique_with_different_flags_set() {
        String expression = "c[ft_i]";
        ConfigurationRegistry.initialize(ALL_ENABLED);
        // will store in cache
        UCUMService.validate(expression);
        // simulate dev changing property
        ConfigurationRegistry.initialize(DISABLE_PREFIX_ON_NON_METRIC);
        Validator.ValidationResult valResult = UCUMService.validate(expression);
        assertThat(valResult).isInstanceOf(Validator.Failure.class);
        System.out.println(ConfigurationRegistry.get());
        System.out.println(PersistenceRegistry.cache.isEnabled());
        Validator.ValidationResult allEnabledValResult = PersistenceRegistry.getInstance().getValidated(new ValKey(expression, ALL_ENABLED.asFeatureFlags()));
        assertThat(allEnabledValResult).isInstanceOf(Validator.Success.class);
    }

    @Test
    public void canon_key_is_unique_with_different_flags_set() {
        String fromExpression = "mol";
        String toExpression = "g";
        ConfigurationRegistry.initialize(ALL_ENABLED);
        // will store in cache
        Converter.ConversionResult conversionResult = UCUMService.convert("1", fromExpression, toExpression, "5");
        assertThat(conversionResult).isInstanceOf(Converter.Success.class);
        // simulate dev changing property
        ConfigurationRegistry.initialize(DISABLE_MOL_MASS_CONVERSION);
        Converter.ConversionResult convResult = UCUMService.convert("1", fromExpression, toExpression, "5");
        assertThat(convResult).isInstanceOf(Converter.FailedConversion.class);
    }
}
