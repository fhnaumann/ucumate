package io.github.fhnaumann;

import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.providers.SQLitePersistenceProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * @author Felix Naumann
 */
public class SQLitePersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    private Connection connection;

    @Override
    protected void registerPersistenceProvider() {
        System.out.println(Paths.get(System.getProperty("user.dir")).resolve("ucumate.db"));
        SQLitePersistenceProvider provider = PersistenceProviderFactory.createDefaultSQLiteProvider();
        connection = provider.connection;

        PersistenceRegistry.getInstance().close(); // ensure old state is cleared
        PersistenceRegistry.register("sqlite", provider); // re-register per test
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
