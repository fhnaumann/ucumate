package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public interface QuickParse {

    /**
     * Internal use only!
     * @param input The input to parse.
     * @return The parsed input or an error was thrown.
     * @throws io.github.fhnaumann.funcs.Validator.ParserException If an error was encountered.
     */
    UCUMExpression.Term parseOrError(String input);
}
