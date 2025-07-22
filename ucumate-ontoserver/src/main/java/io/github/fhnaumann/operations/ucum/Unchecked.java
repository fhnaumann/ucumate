package io.github.fhnaumann.operations.ucum;

import au.csiro.ontoserver.OntoOperationPlugin;
import au.csiro.ontoserver.exceptions.PluginUnprocessableEntityException;

/**
 * @author Felix Naumann
 */
public class Unchecked {

    public static class UncheckedUnprocessableEntityException extends WrappingCheckedException {

        public UncheckedUnprocessableEntityException(String message, OntoOperationPlugin causer) {
            super(new PluginUnprocessableEntityException(message, causer));
        }
    }
}
