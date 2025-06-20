package io.github.fhnaumann;

import com.mongodb.client.MongoClient;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.providers.MongoDBPersistenceProvider;
import io.github.fhnaumann.providers.MySQLPersistenceProvider;
import io.github.fhnaumann.providers.PostgresPersistenceProvider;
import io.github.fhnaumann.providers.SQLitePersistenceProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
public class PersistenceProviderFactory {

    public static PostgresPersistenceProvider createPostgres(String jdbcUrl, String username, String password) {
        HikariDataSource ds = ConnectionPoolFactory.getOrCreate(jdbcUrl, username, password);
        try {
            return new PostgresPersistenceProvider(ds.getConnection(), null, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static MySQLPersistenceProvider createMySQL(String jdbcUrl, String username, String password) {
        HikariDataSource ds = ConnectionPoolFactory.getOrCreate(jdbcUrl, username, password);
        try {
            return new MySQLPersistenceProvider(ds.getConnection(), null, null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static SQLitePersistenceProvider createDefaultSQLiteProvider() {
        Path defaultSQLitePath = Paths.get(ConfigurationRegistry.get().getSqliteDBPath());
        String jdbcUrl = "jdbc:sqlite:" + defaultSQLitePath.toAbsolutePath();
        HikariDataSource ds = ConnectionPoolFactory.getOrCreate(jdbcUrl, "", "");
        try {
            return new SQLitePersistenceProvider(ds.getConnection(), null, null);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create SQLitePersistenceProvider", e);
        }
    }


    private static Path resolveSQLitePath() {
        try {
            // Path to same folder as JAR
            File jarFile = new File(SQLitePersistenceProvider.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());

            Path jarDir = jarFile.isFile() ? jarFile.getParentFile().toPath() : jarFile.toPath();
            return jarDir.resolve("ucumate.db");

        } catch (Exception e) {
            throw new RuntimeException("Could not resolve path for SQLite DB", e);
        }
    }


    public static MongoDBPersistenceProvider createMongoDB(MongoClient client) {
        return new MongoDBPersistenceProvider(client, "ucumate");
    }
}
