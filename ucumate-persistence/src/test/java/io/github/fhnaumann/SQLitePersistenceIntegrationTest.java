package io.github.fhnaumann;

import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.providers.SQLitePersistenceProvider;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * @author Felix Naumann
 */
public class SQLitePersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    private Connection connection;

    @TempDir
    private static Path dbPath;

    @Override
    protected void registerPersistenceProvider() {
        System.setProperty("ucumate.persistence.sqlite.enable", "false");
        System.setProperty("ucumate.persistence.sqlite.dbpath", dbPath.resolve("ucumate.db").toString());
        SQLitePersistenceProvider provider = PersistenceProviderFactory.createDefaultSQLiteProvider();
        connection = provider.connection;

        //PersistenceRegistry.getInstance().close(); // ensure old state is cleared
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
