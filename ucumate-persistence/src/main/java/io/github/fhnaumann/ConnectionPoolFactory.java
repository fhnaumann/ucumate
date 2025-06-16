package io.github.fhnaumann;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionPoolFactory {

    private static final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();

    public static synchronized HikariDataSource getOrCreate(String jdbcUrl, String username, String password) {
        String key = jdbcUrl + "|" + username;
        HikariDataSource existing = pools.get(key);

        if (existing != null) {
            if (existing.isClosed()) {
                pools.remove(key);
                existing.close();
            } else {
                return existing;
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setPoolName("UCUMatePool-" + key.hashCode());

        HikariDataSource newDs = new HikariDataSource(config);
        pools.put(key, newDs);
        return newDs;
    }


    public static void shutdownAll() {
        pools.values().forEach(HikariDataSource::close);
    }
}
