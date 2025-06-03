package me.fhnau.org;

import com.zaxxer.hikari.HikariDataSource;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.persistence.InMemoryCanonicalizePersistenceProvider;
import me.fhnau.org.persistence.PersistenceRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Felix Naumann
 */
public class Main {
    public static void main(String[] args) throws SQLException {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.out.println("Hello, World!");
        String jdbcUrl = "jdbc:postgresql://localhost:5432/ucumate_persistence";
        String username = "felixnaumann";
        String pw = "";
        /*
        try(Connection connection = DriverManager.getConnection(jdbcUrl, username, pw)) {
            PersistenceRegistry.register("postgres", new PostgresPersistenceProvider(connection, null, null));
        }*/


        PersistenceRegistry.disableInMemoryCache(true);

        HikariDataSource ds = ConnectionPoolFactory.getOrCreate(jdbcUrl, username, pw);
        PersistenceRegistry.register("postgres", new PostgresPersistenceProvider(ds.getConnection(), null, null));

        UCUMService.validate("m");
        UCUMService.validate("m");
        UCUMService.validate("m2");
        //System.out.println(UCUMService.canonicalize("5.Cel"));
    }
}