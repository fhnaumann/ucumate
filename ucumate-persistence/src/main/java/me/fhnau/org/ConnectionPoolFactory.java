package me.fhnau.org;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPoolFactory {

    private static final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();

    public static synchronized HikariDataSource getOrCreate(String jdbcUrl, String username, String password) {
        String key = jdbcUrl + "|" + username;

        return pools.computeIfAbsent(key, k -> {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            config.setPoolName("UCUMatePool-" + key.hashCode());

            return new HikariDataSource(config);
        });
    }

    public static void shutdownAll() {
        pools.values().forEach(HikariDataSource::close);
    }
}
