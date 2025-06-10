package io.github.fhnaumann;

import io.github.fhnaumann.providers.SQLitePersistenceProvider;

import java.sql.*;

/**
 * @author Felix Naumann
 */
public class SQLitePersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    private Connection connection;

    @Override
    protected void registerPersistenceProvider() {
        SQLitePersistenceProvider provider = PersistenceProviderFactory.createDefaultSQLiteProvider();
        connection = provider.connection;
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
