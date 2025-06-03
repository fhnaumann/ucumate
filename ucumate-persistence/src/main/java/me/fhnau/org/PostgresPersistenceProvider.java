package me.fhnau.org;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Felix Naumann
 */
public class PostgresPersistenceProvider extends JDBCPersistenceProvider {

    public PostgresPersistenceProvider(Connection connection, String canonicalTableName, String validateTableName) {
        super(connection, canonicalTableName, validateTableName);
    }

    @Override
    public void createCanonicalTable() {
        executeSQLFile("postgres/canonical_table.sql");
    }

    @Override
    public void createValidateTable() {
        executeSQLFile("postgres/validate_table.sql");
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
