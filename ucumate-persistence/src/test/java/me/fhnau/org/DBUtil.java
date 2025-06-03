package me.fhnau.org;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
public class DBUtil {

    static void printCanonicalContents(Connection connection) {
        try (var stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM ucumate_canonical");
            System.out.println("=== ucumate_canonical contents ===");
            while (rs.next()) {
                String key = rs.getString("unit_key");
                String magnitude = rs.getString("magnitude");
                String cfPrefix = rs.getString("cfPrefix");
                String term = rs.getString("term");
                boolean special = rs.getBoolean("special");
                String specialName = rs.getString("specialName");
                String specialUnit = rs.getString("specialUnit");
                String specialValue = rs.getString("specialValue");

                System.out.printf(
                        "key=%s | magnitude=%s | cfPrefix=%s | term=%s | special=%s | specialName=%s | specialUnit=%s | specialValue=%s%n",
                        key, magnitude, cfPrefix, term, special, specialName, specialUnit, specialValue
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void printValidationContents(Connection connection) {
        try (var stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM ucumate_validate");
            System.out.println("Validation Cache Contents:");
            while (rs.next()) {
                String key = rs.getString("unit_key");
                boolean valid = rs.getBoolean("valid");
                System.out.printf(
                        "key=%s | valid=%s",
                        key,
                        valid
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to print validation contents", e);
        }
    }
}
