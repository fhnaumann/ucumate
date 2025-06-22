package io.github.fhnaumann.funcs;

import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public interface PrinterService extends QuickParse {
    /**
     * Creates a string representation of a given UCUMExpression as a string.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms) as a string. Will be validated first.
     * @param printType The type of the printer.
     * @return A String representation of the given UCUMExpression.
     *
     * @throws io.github.fhnaumann.funcs.Validator.ParserException A ParserException if the input could not be parsed.
     *
     * @see Printer.PrintType
     * @see UCUMService#print(UCUMExpression, Printer.PrintType)
     * @see UCUMService#print(String)
     * @see UCUMService#print(UCUMExpression)
     */
    public default String print(String ucumExpression, Printer.PrintType printType) {
        return print(parseOrError(ucumExpression), printType);
    }

    /**
     * Creates a string representation of a given UCUMExpression as a string.
     * <br>
     * Uses the default UCUM syntax printer.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms).
     * @return A String representation of the given UCUMExpression that is a valid UCUM code.
     *
     * @throws io.github.fhnaumann.funcs.Validator.ParserException A ParserException if the input could not be parsed.
     *
     * @see UCUMService#print(String, Printer.PrintType)
     * @see UCUMService#print(UCUMExpression, Printer.PrintType)
     * @see UCUMService#print(UCUMExpression)
     */
    public default String print(String ucumExpression) {
        return print(parseOrError(ucumExpression));
    }

    /**
     * Creates a string representation of a given UCUMExpression.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms).
     * @param printType The type of the printer.
     * @return A String representation of the given UCUMExpression.
     *
     * @see Printer.PrintType
     * @see UCUMService#print(String, Printer.PrintType)
     * @see UCUMService#print(String)
     * @see UCUMService#print(UCUMExpression)
     */
    public String print(UCUMExpression ucumExpression, Printer.PrintType printType);
        // return printers.get(printType).print(ucumExpression);

    /**
     * Creates a string representation of a given UCUMExpression.
     * <br>
     * Uses the default UCUM syntax printer.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms).
     * @return A String representation of the given UCUMExpression that is a valid UCUM code.
     *
     * @see UCUMService#print(String, Printer.PrintType)
     * @see UCUMService#print(UCUMExpression, Printer.PrintType)
     * @see UCUMService#print(String)
     */
    public default String print(UCUMExpression ucumExpression) {
        return print(ucumExpression, Printer.PrintType.UCUM_SYNTAX);
    }

    public default String print(UCUMExpression ucumExpression, Printer printer) {
        return printer.print(ucumExpression);
    }

    public default String print(String ucumExpression, Printer printer) {
        return printer.print(parseOrError(ucumExpression));
    }

}
