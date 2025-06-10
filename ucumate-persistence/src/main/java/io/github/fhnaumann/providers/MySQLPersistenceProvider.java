package io.github.fhnaumann.providers;

import java.sql.Connection;

/**
 * @author Felix Naumann
 */
public class MySQLPersistenceProvider extends JDBCPersistenceProvider {

    public MySQLPersistenceProvider(Connection connection, String canonicalTableName, String validateTableName) {
        super(connection, canonicalTableName, validateTableName);
    }

    @Override
    public void createCanonicalTable() {
        executeSQLFile("mysql/canonical_table.sql");
    }

    @Override
    public void createValidateTable() {
        executeSQLFile("mysql/validate_table.sql");
    }

    @Override
    public String getCanonicalUpsertQuery() {
        return "INSERT INTO " + canonicalTableName + " " +
                "(unit_key, magnitude, cfPrefix, term, special, specialName, specialUnit, specialValue) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "magnitude = VALUES(magnitude), " +
                "cfPrefix = VALUES(cfPrefix), " +
                "term = VALUES(term), " +
                "special = VALUES(special), " +
                "specialName = VALUES(specialName), " +
                "specialUnit = VALUES(specialUnit), " +
                "specialValue = VALUES(specialValue)";
    }

    @Override
    public String getValidateUpsertQuery() {
        return "INSERT INTO " + validateTableName + " (unit_key, valid) " +
                "VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "valid = VALUES(valid)";
    }
}
