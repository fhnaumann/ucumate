package io.github.fhnaumann;

import com.zaxxer.hikari.HikariDataSource;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.providers.SQLitePersistenceProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
public class PersistenceProviderFactory {

    private PersistenceProviderFactory() {
    }

    public static SQLitePersistenceProvider createDefaultSQLiteProvider() {
        Path defaultSQLitePath = Paths.get(ConfigurationRegistry.get().getSqliteDBPath());
        String jdbcUrl = "jdbc:sqlite:" + defaultSQLitePath.toAbsolutePath();
        HikariDataSource ds = ConnectionPoolFactory.getOrCreate(jdbcUrl, "", "");
        try {
            return new SQLitePersistenceProvider(ds.getConnection(), null, null);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create SQLitePersistenceProvider", e);
        }
    }
}
