package me.fhnau.org;

import me.fhnau.org.funcs.*;
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

public class UCUMService {

    private static final Map<PrintType, Printer> printers = Map.of(
        PrintType.UCUM_SYNTAX, new UCUMSyntaxPrinter(),
        PrintType.EXPRESSIVE_UCUM_SYNTAX, new ExpressiveUCUMSyntaxPrinter(),
        PrintType.WOLFRAM_ALPHA_SYNTAX, new WolframAlphaSyntaxPrinter()
    );

    public static ValidationResult validate(String input) {
        return Validator.validate(input);
    }

    public static List<ValidationResult> batchValidate(List<String> inputs) {
        return inputs.stream()
            .map(s -> CompletableFuture.supplyAsync(() -> validate(s), UCUMEngine.getExecutor()))
            .map(CompletableFuture::join)
            .toList();
    }

    public static boolean validateToBool(String input) {
        return switch (validate(input)) {
            case Success success -> true;
            case Failure failure -> false;
        };
    }

    public static ConversionResult convert(UCUMExpression.Term from, UCUMExpression.Term to) {
        return convert(PreciseDecimal.ONE, from, to);
    }

    public static ConversionResult convert(PreciseDecimal factor, UCUMExpression.Term from, UCUMExpression.Term to) {
        return new Converter().convert(new Conversion(factor, from), to);
    }

    public static RelationChecker.RelationResult checkRelation(UCUMExpression.Term term1, UCUMExpression.Term term2) {
        return RelationChecker.checkRelation(term1, term2);
    }

    public static RelationChecker.CommensurableResult checkCommensurable(UCUMExpression.Term term1, UCUMExpression.Term term2) {
        return RelationChecker.checkCommensurable(term1, term2);
    }

    public static Canonicalizer.CanonicalizationResult canonicalize(UCUMExpression.Term term) {
        return new Canonicalizer().canonicalize(term);
    }

    public static String print(UCUMExpression UCUMExpression, PrintType printType) {
        return printers.get(printType).print(UCUMExpression);
    }

    public static String print(UCUMExpression UCUMExpression) {
        return print(UCUMExpression, PrintType.UCUM_SYNTAX);
    }
}
