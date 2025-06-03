package me.fhnau.org;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.persistence.PersistenceRegistry;
import me.fhnau.org.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Stream;

import static me.fhnau.org.TestUtil.parse;
import static me.fhnau.org.TestUtil.print;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Felix Naumann
 */
@Testcontainers
public class PostgresPersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ucumate_persistence")
            .withUsername("test")
            .withPassword("test");

    Connection connection;

    @Override
    protected void registerPersistenceProvider() {
        try {
            connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );
            PersistenceRegistry.register("postgres", new PostgresPersistenceProvider(connection, null, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void clearDatabaseState() {
        try {
            connection = DriverManager.getConnection(
                    postgres.getJdbcUrl(),
                    postgres.getUsername(),
                    postgres.getPassword()
            );
            try (var stmt = connection.createStatement()) {
                stmt.execute("TRUNCATE TABLE ucumate_canonical");
                stmt.execute("TRUNCATE TABLE ucumate_validate");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
