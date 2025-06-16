package io.github.fhnaumann;

import io.github.fhnaumann.compounds.CompoundProvider;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.persistence.PersistenceProvider;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * @author Felix Naumann
 */
public class SQLitePersistenceProviderSPITest {

    @TempDir
    private Path sqliteFile;

    String dbPath;

    @BeforeEach
    public void setup() throws IOException {
        dbPath = sqliteFile.resolve("ucumate.db").toString();
        System.setProperty("ucumate.persistence.sqlite.dbpath", dbPath);
        PersistenceRegistry.searchSPI();
    }

    @AfterEach
    public void teardown() {
        ConnectionPoolFactory.shutdownAll();
        PersistenceRegistry.getInstance().close();
    }

    @Test
    @Disabled("I am too dumb to figure this out. Running these tests one by one works but running the entire class fails.\n" +
            "This is because how SPI discovers and registers stuff in the PersistenceRegistry. But despite every effort to reset\n" +
            "the state in the teardown method, it just does not work. So running the test manually works fine, just together it does not and I give up.")
    public void test_spi_provider_is_auto_discovered() {
        ServiceLoader<PersistenceProvider> loader = ServiceLoader.load(PersistenceProvider.class);
        List<PersistenceProvider> providers = StreamSupport.stream(loader.spliterator(), false).toList();

        assertThat(providers).isNotEmpty();
        assertThat(providers.get(0).getClass().getName()).contains("SQLitePersistenceProvider");
    }

    @Test
    public void test_spi_provider_is_auto_discovered_when_accessing_UCUMService() {
        Validator.validate("[ft_i]");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ucumate_validate");
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(1);
        } catch (SQLException e) {
            fail("Failed to connect to SQLite DB: " + e.getMessage());
        }

        UCUMService.convert("N", "kg.m.s2");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ucumate_canonical");
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(1);
        } catch (SQLException e) {
            fail("Failed to connect to SQLite DB: " + e.getMessage());
        }
    }
}
