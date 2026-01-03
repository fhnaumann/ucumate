package io.github.fhnaumann;

import com.github.dockerjava.api.DockerClient;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.providers.MySQLPersistenceProvider;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
@Testcontainers
public class MysqlPersistenceIntegrationTest extends DBPersistenceIntegrationTestBase {

    @Test
    void debugDockerConnection() {
        System.out.println("=== Environment ===");
        System.out.println("DOCKER_HOST: " + System.getenv("DOCKER_HOST"));
        System.out.println("User: " + System.getProperty("user.name"));

        System.out.println("\n=== Testcontainers Detection ===");
        try {
            DockerClientFactory factory = DockerClientFactory.instance();
            System.out.println("Docker available: " + factory.isDockerAvailable());

            // Try to get the client directly to see the error
            DockerClient client = factory.client();
            System.out.println("Client: " + client);
            client.pingCmd().exec();
            System.out.println("Ping successful!");
        } catch (Exception e) {
            System.out.println("ERROR:");
            e.printStackTrace();
        }
    }

    @Override
    protected void registerPersistenceProvider() {

    }

    @Override
    protected void clearDatabaseState() {

    }
//
//    @Container
//    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8")
//            .withDatabaseName("ucumate_persistence")
//            .withUsername("test")
//            .withPassword("test");
//
//    Connection connection;
//
//    @Override
//    protected void registerPersistenceProvider() {
//        try {
//            connection = DriverManager.getConnection(
//                    mysql.getJdbcUrl(),
//                    mysql.getUsername(),
//                    mysql.getPassword()
//            );
//            PersistenceRegistry.register("mysql", new MySQLPersistenceProvider(connection, null, null));
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected void clearDatabaseState() {
//        try {
//            connection = DriverManager.getConnection(
//                    mysql.getJdbcUrl(),
//                    mysql.getUsername(),
//                    mysql.getPassword()
//            );
//            try (var stmt = connection.createStatement()) {
//                stmt.execute("TRUNCATE TABLE ucumate_canonical");
//                stmt.execute("TRUNCATE TABLE ucumate_validate");
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
