package me.fhnau.org;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.persistence.PersistenceRegistry;
import me.fhnau.org.util.PreciseDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.stream.Stream;

import static me.fhnau.org.TestUtil.parse;
import static me.fhnau.org.TestUtil.print;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Felix Naumann
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DBPersistenceIntegrationTestBase {

    protected abstract void registerPersistenceProvider();
    protected abstract void clearDatabaseState();

    @BeforeEach
    public void setup() throws SQLException {
        //connection = getConnection();
        PersistenceRegistry.disableInMemoryCache(true);
        //PersistenceRegistry.register("postgres", new PostgresPersistenceProvider(connection, null, null));
        registerPersistenceProvider();

        clearDatabaseState();
    }

    @AfterEach
    public void cleanup() {
        PersistenceRegistry.getInstance().close();
    }


    @Test
    public void can_persist_canonicalization() {
        UCUMExpression.Term parsedTerm = ((Validator.Success) UCUMService.validate("g")).term();
        ((Canonicalizer.Success) UCUMService.canonicalize(parsedTerm)).canonicalTerm();
        Canonicalizer.CanonicalStepResult canonicalStepResult = PersistenceRegistry.getInstance().getCanonical(parsedTerm);
        assertThat(canonicalStepResult).isNotNull();
        assertThat("g+1").isEqualTo(UCUMService.print(canonicalStepResult.term()));
        assertThat(new PreciseDecimal("1")).isEqualTo(canonicalStepResult.magnitude());
        assertThat(new PreciseDecimal("1")).isEqualTo(canonicalStepResult.cfPrefix());
        assertFalse(canonicalStepResult.specialHandlingActive());
    }

    @Test
    public void can_persist_canonicalization_multiple_steps() {
        UCUMService.canonicalize("S");
        Canonicalizer.CanonicalStepResult canonicalStepResult = PersistenceRegistry.getInstance().getCanonical(parse("S"));
        assertThat(canonicalStepResult).isNotNull();
        assertThat("C+2.m-2.s+1.g-1").isEqualTo(UCUMService.print(canonicalStepResult.term()));
        assertThat(new PreciseDecimal("0.001")).isEqualTo(canonicalStepResult.magnitude());
        assertThat(new PreciseDecimal("1")).isEqualTo(canonicalStepResult.cfPrefix());
        assertFalse(canonicalStepResult.specialHandlingActive());
    }

    @Test
    public void can_persist_canonicalization_special_unit() {
        UCUMService.canonicalize("Cel");
        Canonicalizer.CanonicalStepResult canonicalStepResult = PersistenceRegistry.getInstance().getCanonical(parse("Cel"));
        assertEquals("K+1", print(canonicalStepResult.term()));
        assertTrue(canonicalStepResult.specialHandlingActive());
        assertNotNull(canonicalStepResult.specialFunction());
        assertEquals("Cel", canonicalStepResult.specialFunction().name());
        assertEquals("K", canonicalStepResult.specialFunction().unit());
        assertEquals("1", canonicalStepResult.specialFunction().value().toString());
    }

    @ParameterizedTest
    @MethodSource("provideValidationPersistence")
    public void can_persist_validation(String input, boolean valid) {
        UCUMService.validate(input);
        Validator.ValidationResult fromCache = PersistenceRegistry.getInstance().getValidated(input);
        boolean actualValid = switch (fromCache) {
            case Validator.Failure failure -> false;
            case Validator.Success success -> true;
            case null -> fail("Cache return null");
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
