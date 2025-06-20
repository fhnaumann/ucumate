package io.github.fhnaumann.funcs;

import io.github.fhnaumann.compounds.CompoundUtil;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

/**
 * @author Felix Naumann
 */
public interface ConverterService extends QuickParse {

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>factor * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param factor The factor for the <code>from</code> term.
     * @param from The term that is converted from as a string. Will be validated first.
     * @param to The term that is converted to as a string. Will be validated first.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     *
     * @see Converter.ConversionResult
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public default Converter.ConversionResult convert(PreciseDecimal factor, String from, String to) {
        try {
            return convert(factor, parseOrError(from), parseOrError(to));
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
        }
    }

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>factor * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param factor The factor for the <code>from</code> term as a string. Will be converted to a {@link PreciseDecimal}.
     * @param from The term that is converted from as a string. Will be validated first.
     * @param to The term that is converted to as a string. Will be validated first.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     *
     * @see Converter.ConversionResult
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(String, String)
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public default Converter.ConversionResult convert(String factor, String from, String to) {
        return convert(new PreciseDecimal(factor), from, to);
    }

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>1 * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param from The term that is converted from as a string. Will be validated first.
     * @param to The term that is converted to as a string. Will be validated first.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     *
     * @see Converter.ConversionResult
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public default Converter.ConversionResult convert(String from, String to) {
        return convert(PreciseDecimal.ONE, from, to);
    }

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>1 * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param from The term that is converted from.
     * @param to The term that is converted to.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     *
     * @see Converter.ConversionResult
     * @see UCUMService#convert(String, String)
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     */
    public default Converter.ConversionResult convert(UCUMExpression.Term from, UCUMExpression.Term to) {
        return convert(PreciseDecimal.ONE, from, to);
    }

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>factor * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param factor The factor for the <code>from</code> term.
     * @param from The term that is converted from.
     * @param to The term that is converted to.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     *
     * @see Converter.ConversionResult
     * @see UCUMService#convert(String, String)
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public default Converter.ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to) {
        return convert(factor, from, to, null);
    }

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>1 * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param factor The factor for the <code>from</code> term.
     * @param from The term that is converted from as a string. Will be validated first.
     * @param to The term that is converted to as a string. Will be validated first.
     * @param substanceMolMassCoeff The additional substance's molar mass coefficient. If not null (and globally enabled), then it will convert mole to a mass unit.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     */
    public default Converter.ConversionResult convert(String factor, String from, String to, String substanceMolMassCoeff) {
        /*
        The string may optionally be something other than a number. In this case the current CompoundProvider is asked for a mapping to a number.
         */
        substanceMolMassCoeff = CompoundUtil.resolveMolarMass(substanceMolMassCoeff);
        try {
            return convert(new PreciseDecimal(factor), parseOrError(from), parseOrError(to), substanceMolMassCoeff != null ? new PreciseDecimal(substanceMolMassCoeff) : null);
        } catch (Validator.ParserException e) {
            return new Validator.ParserError();
        }

    }

    /**
     * Convert a UCUMTerm to another UCUMTerm.
     * Essentially solves
     * <code>1 * from = x * to</code> and returns <code>x</code>. Or it fails with additional information provided in the return object.
     *
     * @param factor The factor for the <code>from</code> term.
     * @param from The term that is converted from as a string. Will be validated first.
     * @param to The term that is converted to as a string. Will be validated first.
     * @param substanceMolMassCoeff The additional substance's molar mass coefficient. If not null (and globally enabled), then it will convert mole to a mass unit.
     * @return A ConversionResult either containing the resulting conversion factor or an error with more details.
     */
    public Converter.ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to, PreciseDecimal substanceMolMassCoeff);
}
