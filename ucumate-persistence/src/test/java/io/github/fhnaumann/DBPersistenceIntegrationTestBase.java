package io.github.fhnaumann;

import io.github.fhnaumann.configuration.CanonKey;
import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.configuration.ValKey;
import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.PreciseDecimal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static io.github.fhnaumann.TestUtil.parse;
import static io.github.fhnaumann.TestUtil.print;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Felix Naumann
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DBPersistenceIntegrationTestBase {

    protected abstract void registerPersistenceProvider();
    protected abstract void clearDatabaseState();

    private static PrinterService printerService;
    private static ValidatorService validatorService;
    private static CanonicalizerService canonicalizerService;

    @BeforeAll
    public static void init() {
        ConfigurationRegistry.initialize(Configuration.builder().withUCUMVersion(UcumVersion.getLatest().getVersion()).build());
        printerService = new Printer();
        validatorService = new Validator();
        canonicalizerService = new Canonicalizer(printerService, validatorService);
    }

    @BeforeEach
    public void setup() throws SQLException {
        ConfigurationRegistry.initialize(Configuration.builder().enableSQLitePersistence(false).withUCUMVersion(UcumVersion.V2_2.getVersion()).build());
        ConnectionPoolFactory.shutdownAll();
        registerPersistenceProvider();
        PersistenceRegistry.disableInMemoryCache(true);
        clearDatabaseState();
    }

    @AfterEach
    public void cleanup() {
        PersistenceRegistry.getInstance().close();
    }


    @Test
    public void can_persist_canonicalization() {
        UCUMExpression.Term parsedTerm = ((Validator.Success) validatorService.validate("g")).term();
        ((CanonicalizerService.Success) canonicalizerService.canonicalize(parsedTerm)).canonicalTerm();
        Canonicalizer.CanonicalStepResult canonicalStepResult = PersistenceRegistry.getInstance().getCanonical(CanonKey.of(parsedTerm, UcumVersion.getLatest()));
        assertThat(canonicalStepResult).isNotNull();
        assertThat(printerService.print(canonicalStepResult.term())).isEqualTo("g");
        assertThat(new PreciseDecimal("1")).isEqualTo(canonicalStepResult.magnitude());
        assertThat(new PreciseDecimal("1")).isEqualTo(canonicalStepResult.cfPrefix());
        assertFalse(canonicalStepResult.specialHandlingActive());
    }

    @Test
    public void can_persist_canonicalization_multiple_steps() {
        canonicalizerService.canonicalize("S");
        Canonicalizer.CanonicalStepResult canonicalStepResult = PersistenceRegistry.getInstance().getCanonical(CanonKey.of(parse("S"), UcumVersion.getLatest()));
        assertThat(canonicalStepResult).isNotNull();
        assertThat(printerService.print(canonicalStepResult.term())).isEqualTo("C+2.g-1.m-2.s");
        assertThat(new PreciseDecimal("0.001")).isEqualTo(canonicalStepResult.magnitude());
        assertThat(new PreciseDecimal("1")).isEqualTo(canonicalStepResult.cfPrefix());
        assertFalse(canonicalStepResult.specialHandlingActive());
    }

    @Test
    public void can_persist_canonicalization_special_unit() {
        canonicalizerService.canonicalize("Cel");
        Canonicalizer.CanonicalStepResult canonicalStepResult = PersistenceRegistry.getInstance().getCanonical(CanonKey.of(parse("Cel"), UcumVersion.getLatest()));
        assertEquals("K", print(canonicalStepResult.term()));
        assertTrue(canonicalStepResult.specialHandlingActive());
        assertNotNull(canonicalStepResult.specialFunction());
        assertEquals("Cel", canonicalStepResult.specialFunction().name());
        assertEquals("K", canonicalStepResult.specialFunction().unit());
        assertEquals("1", canonicalStepResult.specialFunction().value().toString());
    }

    @ParameterizedTest
    @MethodSource("provideValidationPersistence")
    public void can_persist_validation(String input, boolean valid) {
        validatorService.validate(input);
        Validator.ValidationResult fromCache = PersistenceRegistry.getInstance().getValidated(ValKey.of(input, UcumVersion.V2_2));
        boolean actualValid = switch (fromCache) {
            case Validator.Failure failure -> false;
            case Validator.Success success -> true;
            case null -> fail("Storage return null");
        };
        assertEquals(valid, actualValid);
    }

    private static Stream<Arguments> provideValidationPersistence() {
        return Stream.of(
                Arguments.of("m", true),
                Arguments.of("cm", true),
                Arguments.of("[ft_i]", true),
                Arguments.of("invalid", false)
        );
    }
}
