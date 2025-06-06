package me.fhnau.org.providers;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.funcs.printer.Printer;
import me.fhnau.org.model.UCUMDefinition;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.persistence.PersistenceProvider;
import me.fhnau.org.util.PreciseDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Felix Naumann
 */
public abstract class JDBCPersistenceProvider implements PersistenceProvider {

    private static final Logger logger = LoggerFactory.getLogger(JDBCPersistenceProvider.class);

    public final Connection connection;
    protected final String canonicalTableName;
    protected final String validateTableName;

    public JDBCPersistenceProvider(Connection connection, String canonicalTableName, String validateTableName) {
        this.connection = connection;
        this.canonicalTableName = canonicalTableName != null ? canonicalTableName : "ucumate_canonical";
        this.validateTableName = validateTableName != null ? validateTableName : "ucumate_validate";
        createCanonicalTable();
        createValidateTable();
    }

    public abstract void createCanonicalTable();
    public abstract void createValidateTable();
    public abstract String getCanonicalUpsertQuery();
    public abstract String getValidateUpsertQuery();

    protected void executeSQLFile(String path) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
            if (input == null) throw new RuntimeException("Missing SQL file: " + path);
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to execute SQL file: " + path, e);
        }
    }


    @Override
    public void saveCanonical(UCUMExpression key, Canonicalizer.CanonicalStepResult value) {
        try (PreparedStatement stmt = connection.prepareStatement(getCanonicalUpsertQuery())) {
            String keyString  = UCUMService.print(key, Printer.PrintType.UCUM_SYNTAX);
            stmt.setString(1, keyString);
            stmt.setString(2, value.magnitude().toString());
            stmt.setString(3, value.cfPrefix().toString());
            String canonStep = UCUMService.print(value.term(), Printer.PrintType.UCUM_SYNTAX);
            stmt.setString(4, canonStep);
            stmt.setBoolean(5, value.specialHandlingActive());
            if(value.specialHandlingActive()) {
                stmt.setString(6, value.specialFunction().name());
                stmt.setString(7, value.specialFunction().unit());
                stmt.setString(8, value.specialFunction().value().toString());
            }
            else {
                stmt.setNull(6, Types.VARCHAR);
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.VARCHAR);
            }
            stmt.executeUpdate();
            logger.debug("Saved key={}, canonStep={} to {}.", keyString, canonStep, connection.getMetaData());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save canonical data", e);
        }
    }

    @Override
    public Canonicalizer.CanonicalStepResult getCanonical(UCUMExpression key) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM " + canonicalTableName + " WHERE unit_key = ?")) {
            String keyString = UCUMService.print(key, Printer.PrintType.UCUM_SYNTAX);
            stmt.setString(1, keyString);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PreciseDecimal magnitude = new PreciseDecimal(rs.getString("magnitude"));
                PreciseDecimal cfPrefix = new PreciseDecimal(rs.getString("cfPrefix"));
                UCUMExpression.Term term = Validator.parseCanonical(rs.getString("term"));
                boolean special = rs.getBoolean("special");
                UCUMDefinition.UCUMFunction ucumFunction = null;
                if(special) {
                    ucumFunction = new UCUMDefinition.UCUMFunction(
                            rs.getString("specialName"),
                            new PreciseDecimal(rs.getString("specialValue")),
                            rs.getString("specialUnit")
                    );
                }
                Canonicalizer.CanonicalStepResult canonicalStepResult = new Canonicalizer.CanonicalStepResult(term, magnitude, cfPrefix, special, ucumFunction);
                logger.debug("Read from {}: key={}, canonStep={}", connection.getMetaData(), keyString, rs.getString("term"));
                return canonicalStepResult;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load canonical", e);
        }
    }

    @Override
    public void saveValidated(String key, Validator.ValidationResult value) {
        try (PreparedStatement stmt = connection.prepareStatement(getValidateUpsertQuery())) {
            stmt.setString(1, key);
            stmt.setBoolean(2, switch (value) {
                case Validator.Failure failure -> false;
                case Validator.Success success -> true;
            });
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save validated data", e);
        }
    }


    @Override
    public Validator.ValidationResult getValidated(String key) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT valid FROM " + validateTableName + " WHERE unit_key = ?")) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean valid = rs.getBoolean("valid");
                if(valid) {
                    UCUMExpression.Term parsedKey = Validator.parseByPassChecks(key);
                    return new Validator.Success(parsedKey);
                }
                else {
                    return new Validator.Failure();
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load validated data", e);
        }
    }

    @Override
    public Map<String, Validator.ValidationResult> getAllValidated() {
        Map<String, Validator.ValidationResult> resultMap = new HashMap<>();

        String sql = "SELECT unit_key, valid FROM ucumate_validate";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String key = rs.getString("unit_key");
                boolean valid = rs.getBoolean("valid");

                Validator.ValidationResult result;
                if (valid) {
                    UCUMExpression.Term parsed = Validator.parseByPassChecks(key);
                    result = new Validator.Success(parsed);
                } else {
                    result = new Validator.Failure();
                }
                resultMap.put(key, result);

                logger.debug("Loaded {} from data source into cache", key);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load validation entries", e);
        }

        return resultMap;
    }

    @Override
    public Map<UCUMExpression, Canonicalizer.CanonicalStepResult> getAllCanonical() {
        Map<UCUMExpression, Canonicalizer.CanonicalStepResult> resultMap = new HashMap<>();

        String sql = "SELECT * FROM ucumate_canonical";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String unitKey = rs.getString("unit_key");
                String magnitudeStr = rs.getString("magnitude");
                String cfPrefixStr = rs.getString("cfPrefix");
                String termStr = rs.getString("term");
                boolean special = rs.getBoolean("special");

                UCUMExpression inputKey = Validator.parseByPassChecks(unitKey);
                UCUMExpression.Term term = Validator.parseCanonical(termStr);
                PreciseDecimal magnitude = new PreciseDecimal(magnitudeStr);
                PreciseDecimal cfPrefix = new PreciseDecimal(cfPrefixStr);

                UCUMDefinition.UCUMFunction function = null;
                if (special) {
                    String name = rs.getString("specialName");
                    String unit = rs.getString("specialUnit");
                    String valueStr = rs.getString("specialValue");
                    function = new UCUMDefinition.UCUMFunction(name, new PreciseDecimal(valueStr), unit);
                }

                Canonicalizer.CanonicalStepResult step = new Canonicalizer.CanonicalStepResult(
                        term,
                        magnitude,
                        cfPrefix,
                        special,
                        function
                );

                logger.debug("Loaded {} from data source into cache.", unitKey);

                resultMap.put(inputKey, step);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load canonical entries", e);
        }

        return resultMap;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
