package me.fhnau.org;

import me.fhnau.org.persistence.PersistenceRegistry;
import org.junit.jupiter.api.BeforeAll;

import java.sql.*;
import java.util.Enumeration;

/**
 * @author Felix Naumann
 */
public class SQLitePersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    private Connection connection;

    @Override
    protected void registerPersistenceProvider() {
        SQLitePersistenceProvider provider = PersistenceProviderFactory.createDefaultSQLiteProvider();
        connection = provider.connection;
    }

    @Override
    protected void clearDatabaseState() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM ucumate_canonical;");
            stmt.execute("DELETE FROM ucumate_validate;");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear SQLite tables", e);
        }
    }
}
