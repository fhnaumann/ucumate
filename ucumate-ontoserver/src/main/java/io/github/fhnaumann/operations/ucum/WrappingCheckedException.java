package io.github.fhnaumann.operations.ucum;

import au.csiro.ontoserver.exceptions.PluginBaseException;

/**
 * @author Felix Naumann
 */
public abstract class WrappingCheckedException extends RuntimeException {

    private final PluginBaseException underlyingException;

    public WrappingCheckedException(PluginBaseException underlyingException) {
        this.underlyingException = underlyingException;
    }

    public PluginBaseException getUnderlyingException() {
        return underlyingException;
    }
}
