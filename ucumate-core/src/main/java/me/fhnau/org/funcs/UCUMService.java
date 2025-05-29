package me.fhnau.org.funcs;

import me.fhnau.org.funcs.Canonicalizer.CanonicalizationResult;
import me.fhnau.org.funcs.Converter.Conversion;
import me.fhnau.org.funcs.Converter.ConversionResult;
import me.fhnau.org.funcs.Validator.Failure;
import me.fhnau.org.funcs.Validator.Success;
import me.fhnau.org.funcs.Validator.ValidationResult;
import me.fhnau.org.funcs.printer.ExpressiveUCUMSyntaxPrinter;
import me.fhnau.org.funcs.printer.Printer;
import me.fhnau.org.funcs.printer.Printer.PrintType;
import me.fhnau.org.funcs.printer.UCUMSyntaxPrinter;
import me.fhnau.org.funcs.printer.WolframAlphaSyntaxPrinter;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.util.PreciseDecimal;
import me.fhnau.org.util.UCUMEngine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This class provides all the functionality of the ucumate library in a centralized place.
 * This includes validation, canonicalization, conversion, commensurability, and printing.
 * Each functionality is overloaded to take either a string or an already parsed input.
 *
 * @author Felix Naumann
 */
public class UCUMService {

    private static final Map<PrintType, Printer> printers = Map.of(
        PrintType.UCUM_SYNTAX, new UCUMSyntaxPrinter(),
        PrintType.EXPRESSIVE_UCUM_SYNTAX, new ExpressiveUCUMSyntaxPrinter(),
        PrintType.WOLFRAM_ALPHA_SYNTAX, new WolframAlphaSyntaxPrinter()
    );

    /**
     * Validates a given string.
     * @param input A string containing a potential UCUMTerm.
     * @return A ValidationResult with information about the validity.
     *
     * @see UCUMService#validateToBool(String)
     * @see ValidationResult
     */
    public static ValidationResult validate(String input) {
        return Validator.validate(input);
    }

    /**
     * Batch validate a list of strings. Executes the validation in parallel.
     * @param inputs A list if strings containing potential UCUMTerms.
     * @return A list of ValidationResults with information about each validity.
     *
     * @see UCUMService#validate(String)
     * @see ValidationResult
     */
    public static List<ValidationResult> batchValidate(List<String> inputs) {
        return inputs.stream()
            .map(s -> CompletableFuture.supplyAsync(() -> validate(s), UCUMEngine.getExecutor()))
            .map(CompletableFuture::join)
            .toList();
    }

    /**
     * Validate a given String and return a boolean.
     * @param input A string containing a potential UCUMTerm.
     * @return A boolean that was mapped from {@link ValidationResult} where {@link Success} -> true and {@link Failure} -> false.
     *
     * @see UCUMService#validate(String)
     */
    public static boolean validateToBool(String input) {
        return switch (validate(input)) {
            case Success success -> true;
            case Failure failure -> false;
        };
    }

    private static UCUMExpression.Term parseOrError(String input) {
        return switch (validate(input)) {
            case Success success -> success.term();
            case Failure failure -> throw new RuntimeException("Failed parsing input: %s".formatted(input));
        };
    }

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
     * @see ConversionResult
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public static ConversionResult convert(PreciseDecimal factor, String from, String to) {
        return convert(factor, parseOrError(from), parseOrError(to));
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
     * @see ConversionResult
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#convert(String, String)  
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public static ConversionResult convert(String factor, String from, String to) {
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
     * @see ConversionResult
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term) 
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public static ConversionResult convert(String from, String to) {
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
     * @see ConversionResult
     * @see UCUMService#convert(String, String)
     * @see UCUMService#convert(String, String, String)
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(PreciseDecimal, UCUMExpression.Term, UCUMExpression.Term) 
     */
    public static ConversionResult convert(UCUMExpression.Term from, UCUMExpression.Term to) {
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
     * @see ConversionResult
     * @see UCUMService#convert(String, String) 
     * @see UCUMService#convert(String, String, String) 
     * @see UCUMService#convert(PreciseDecimal, String, String)
     * @see UCUMService#convert(UCUMExpression.Term, UCUMExpression.Term)
     */
    public static ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to) {
        return new Converter().convert(new Conversion(factor, from), to);
    }

    /**
     * Checks the relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @return A RelationResult containing information about the relation between the two terms.
     *
     * @see me.fhnau.org.funcs.RelationChecker.RelationResult
     * @see UCUMService#checkCommensurable(UCUMExpression.Term, UCUMExpression.Term)
     * @see UCUMService#checkCommensurable(String, String)
     */
    public static RelationChecker.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2) {
        return RelationChecker.checkRelation(term1, term2);
    }

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation as a string. Will be validated first.
     * @param term2 The second term in the relation. Will be validated first.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see me.fhnau.org.funcs.RelationChecker.CommensurableResult
     * @see UCUMService#checkCommensurable(UCUMExpression.Term, UCUMExpression.Term)
     */
    public static RelationChecker.CommensurableResult checkCommensurable(String term1, String term2) {
        return checkCommensurable(parseOrError(term1), parseOrError(term2));
    }

    /**
     * Checks the commensurability relation between two UCUMTerms.
     *
     * @param term1 The first term in the relation.
     * @param term2 The second term in the relation.
     * @return A CommensurableResult containing information about the relation between the two terms.
     *
     * @see me.fhnau.org.funcs.RelationChecker.CommensurableResult
     * @see UCUMService#checkCommensurable(String, String)
     */
    public static RelationChecker.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2) {
        return RelationChecker.checkCommensurable(term1, term2);
    }

    /**
     * Canonicalize a UCUMTerm that is given as a string.
     * Canonicalizing includes normalizing the term. The term is simplified as much as possible and is written with only
     * multiplication and exponents.
     * <br>
     * I.e. <code>m/s2</code> becomes <code>m1.s-2</code> or <code>m/(s.m)</code> becomes <code>s-1</code>.
     *
     * @param term A term as a string. Will be validated first
     * @return A CanonicalizationResult either containing the canonical form or an error with more details.
     *
     * @see CanonicalizationResult
     * @see UCUMService#canonicalize(UCUMExpression.Term)
     */
    public static CanonicalizationResult canonicalize(String term) {
        return canonicalize(parseOrError(term));
    }

    /**
     * Canonicalize a UCUMTerm.
     * Canonicalizing includes normalizing the term. The term is simplified as much as possible and is written with only
     * multiplication and exponents.
     * <br>
     * I.e. <code>m/s2</code> becomes <code>m1.s-2</code> or <code>m/(s.m)</code> becomes <code>s-1</code>.
     *
     * @param term A term.
     * @return A CanonicalizationResult either containing the canonical form or an error with more details.
     *
     * @see CanonicalizationResult
     * @see UCUMService#canonicalize(String)
     */
    public static CanonicalizationResult canonicalize(UCUMExpression.Term term) {
        return new Canonicalizer().canonicalize(term);
    }

    /**
     * Creates a string representation of a given UCUMExpression as a string.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms) as a string. Will be validated first.
     * @param printType The type of the printer.
     * @return A String representation of the given UCUMExpression.
     *
     * @see PrintType
     * @see UCUMService#print(UCUMExpression, PrintType)
     * @see UCUMService#print(String)
     * @see UCUMService#print(UCUMExpression)
     */
    public static String print(String ucumExpression, PrintType printType) {
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
     * @see UCUMService#print(String, PrintType)
     * @see UCUMService#print(UCUMExpression, PrintType)
     * @see UCUMService#print(UCUMExpression)
     */
    public static String print(String ucumExpression) {
        return print(parseOrError(ucumExpression));
    }

    /**
     * Creates a string representation of a given UCUMExpression.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms).
     * @param printType The type of the printer.
     * @return A String representation of the given UCUMExpression.
     *
     * @see PrintType
     * @see UCUMService#print(String, PrintType)
     * @see UCUMService#print(String)
     * @see UCUMService#print(UCUMExpression)
     */
    public static String print(UCUMExpression ucumExpression, PrintType printType) {
        return printers.get(printType).print(ucumExpression);
    }

    /**
     * Creates a string representation of a given UCUMExpression.
     * <br>
     * Uses the default UCUM syntax printer.
     *
     * @param ucumExpression Any UCUMExpression (includes Terms).
     * @return A String representation of the given UCUMExpression that is a valid UCUM code.
     *
     * @see UCUMService#print(String, PrintType)
     * @see UCUMService#print(UCUMExpression, PrintType)
     * @see UCUMService#print(String)
     */
    public static String print(UCUMExpression ucumExpression) {
        return print(ucumExpression, PrintType.UCUM_SYNTAX);
    }
}
