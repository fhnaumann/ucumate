package io.github.fhnaumann.configuration;

import io.github.fhnaumann.funcs.PrinterService;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;

/**
 * @author Felix Naumann
 */
public record CanonKey(UCUMExpression expression, FeatureFlags flags) {

    private static final PrinterService printerService = new Printer();

    public String toStorageKey(FeatureFlags flags) {
        return FeatureFlags.toStorageKey(printerService.print(expression, Printer.PrintType.UCUM_SYNTAX), flags);
    }

    public static CanonKey fromStorageKey(String storageKey) {
        ValKey valKey = FeatureFlags.fromStorageKey(storageKey);
        return new CanonKey(Validator.parseByPassChecks(valKey.expression()), valKey.flags());
    }

    public static CanonKey of(UCUMExpression expression) {
        return new CanonKey(expression, FeatureFlagsContext.get());
    }
}
