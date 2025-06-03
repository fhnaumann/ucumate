package me.fhnau.org.persistence;

/**
 * @author Felix Naumann
 */
public interface PersistenceProviderFactory {
    PersistenceProvider create();
    String getName();
}
