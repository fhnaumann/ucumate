package io.github.fhnaumann.persistence;

/**
 * @author Felix Naumann
 */
public interface InMemory {

    boolean isEnabled();
    void setEnabled(boolean enabled);
    void clearCache();
}
