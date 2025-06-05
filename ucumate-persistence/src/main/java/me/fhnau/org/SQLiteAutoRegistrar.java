package me.fhnau.org;

import me.fhnau.org.persistence.PersistenceRegistry;

/**
 * @author Felix Naumann
 */
public class SQLiteAutoRegistrar {
    static {
        if (!PersistenceRegistry.hasAny()) {
            try {
                PersistenceRegistry.register("sqlite", PersistenceProviderFactory.createDefaultSQLiteProvider());
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize default SQLite storage", e);
            }
        }
    }

    // force this class to load
    public static void ensureLoaded() {}
}
