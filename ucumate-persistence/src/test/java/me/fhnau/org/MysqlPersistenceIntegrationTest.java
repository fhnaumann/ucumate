package me.fhnau.org;

import me.fhnau.org.persistence.PersistenceRegistry;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
@Testcontainers
public class MysqlPersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
            .withDatabaseName("ucumate_persistence")
            .withUsername("test")
            .withPassword("test");

    Connection connection;

    @Override
    protected void registerPersistenceProvider() {
        try {
            connection = DriverManager.getConnection(
                    mysql.getJdbcUrl(),
                    mysql.getUsername(),
                    mysql.getPassword()
            );
            PersistenceRegistry.register("mysql", new MySQLPersistenceProvider(connection, null, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void clearDatabaseState() {
        try {
            connection = DriverManager.getConnection(
                    mysql.getJdbcUrl(),
                    mysql.getUsername(),
                    mysql.getPassword()
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
