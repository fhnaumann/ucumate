package io.github.fhnaumann.providers;

import io.github.fhnaumann.ConnectionPoolFactory;
import io.github.fhnaumann.configuration.Configuration;
import io.github.fhnaumann.configuration.ConfigurationRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
public class SQLitePersistenceProvider extends JDBCPersistenceProvider {


    public SQLitePersistenceProvider() throws SQLException {
        super(ConnectionPoolFactory.getOrCreate("jdbc:sqlite:%s".formatted(ConfigurationRegistry.get().getSqliteDBPath()), "", "").getConnection(), null, null);
    }

    public SQLitePersistenceProvider(Connection connection, String canonicalTableName, String validateTableName) {
        super(connection, canonicalTableName, validateTableName);
    }

    @Override
    public void createCanonicalTable() {
        executeSQLFile("sqlite/canonical_table.sql");
    }

    @Override
    public void createValidateTable() {
        executeSQLFile("sqlite/validate_table.sql");
    }

    @Override
    public String getCanonicalUpsertQuery() {
        return "INSERT INTO " + canonicalTableName + " " +
                "(unit_key, magnitude, cfPrefix, term, special, specialName, specialUnit, specialValue) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (unit_key) DO UPDATE SET " +
                "magnitude = EXCLUDED.magnitude, " +
                "cfPrefix = EXCLUDED.cfPrefix, " +
                "term = EXCLUDED.term, " +
                "special = EXCLUDED.special, " +
                "specialName = EXCLUDED.specialName, " +
                "specialUnit = EXCLUDED.specialUnit, " +
                "specialValue = EXCLUDED.specialValue";
    }

    @Override
    public String getValidateUpsertQuery() {
        return "INSERT INTO " + validateTableName + " (unit_key, valid) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (unit_key) DO UPDATE SET " +
                "valid = EXCLUDED.valid";
    }

}
