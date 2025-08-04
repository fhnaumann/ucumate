package io.github.fhnaumann.converter;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.configuration.CacheConfiguration;
import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.funcs.ConverterService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Properties;
import java.util.stream.Stream;

import static io.github.fhnaumann.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {

    private static Converter converter;

    @BeforeAll
    public static void init() {
        Properties prop = new Properties();
        prop.setProperty("ucumate.caching.enable", "true");
        ConfigurationRegistry.initialize(Configuration.builder().enableMolMassConversion(true).cacheConfig(CacheConfiguration.fromProps(prop)).build());
    }

    @BeforeAll
    public static void setUp() {
        converter = new Converter(new Printer(), new Validator());
    }

    @ParameterizedTest
    @MethodSource("provide_mol_mass_conversion")
    public void test_can_convert_mol_to_mass(String factor, String from, String to, String substanceMolarMassCoeff, String expected) {
        Converter.ConversionResult convResult = converter.convert(factor, from, to, substanceMolarMassCoeff);
        assertThat(convResult)
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast)
                .extracting(ConverterService.Success::conversionFactor)
                .asString()
                .startsWith(expected);
    }

    private static Stream<Arguments> provide_mol_mass_conversion() {
        return Stream.of(
                Arguments.of("1", "mol", "g", "5", "5"),
                Arguments.of("3", "5.mol", "2.g", "10", "75"),
                Arguments.of("1", "g", "mol", "5", "0.2"),
                Arguments.of("3", "5.g", "2.mol", "10", "0.75"),
                Arguments.of("1", "mol2", "g2", "5", "25"),
                Arguments.of("3", "5.mol2", "2.g2", "10", "750"),
                Arguments.of("3", "5.g2", "2.mol2", "10", "0.075"),
                Arguments.of("1", "osm", "g", "5", "5"),
                Arguments.of("1", "g", "osm", "5", "0.2"),
                Arguments.of("1", "kat", "g/s", "5", "5"),
                Arguments.of("1", "g/s", "kat", "5", "0.2"),
                //Arguments.of("1", "[pH]", "g/L", "5", "0.5"),
                Arguments.of("1", "mol", "1", null, "602214076000000000000000")
                //Arguments.of("3", "5.[pH]", "2.g/L", "10", "7.5")
        );
    }

    @Test
    public void test_convert() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, inch_term()), meter_term());
        System.out.println(result);
        assert_cf(result, pd_u("0.0254"));
    }

    @Test
    public void inch_to_cm() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, inch_term()), cm_term());
        assert_cf(result, pd_u("2.54"));
    }

    @Test
    public void inch2_to_cm2() {
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(ONE, inch2_term()), cm2_term());
        assert_cf(result, pd_u("6.4516"));
    }

    @Test
    public void mega_inch_to_cm() {
        UCUMExpression.Term mega_inch = SoloTermBuilder.builder().withPrefix(mega, inches).noExpNoAnnot().asTerm().build(); // NOSONAR
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, mega_inch), cm_term());
        assert_cf(result, pd_u("2540000"));
    }

    @Test
    public void mega_inch2_to_dm2() {
        UCUMExpression.Term inch2 = SoloTermBuilder.builder().withPrefix(mega, inches).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        UCUMExpression.Term meter2 = SoloTermBuilder.builder().withPrefix(dezi, meter).asComponent().withExponent(2).withoutAnnotation().asTerm().build();
        Converter.ConversionResult result = converter.convert(new Converter.Conversion(PreciseDecimal.ONE, inch2), meter2);
        assert_cf(result, pd_u("64516000000"));
    }

    private static void assert_cf(Converter.ConversionResult actual, PreciseDecimal expectedCf) {
        assertThat(actual)
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast)
                .extracting(ConverterService.Success::conversionFactor)
                .isEqualTo(expectedCf);
    }
}
