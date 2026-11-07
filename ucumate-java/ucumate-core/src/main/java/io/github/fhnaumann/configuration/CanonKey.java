package io.github.fhnaumann.configuration;

import io.github.fhnaumann.funcs.PrinterService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;

/**
 * @author Felix Naumann
 */
public record CanonKey(UCUMExpression expression, FeatureFlags flags, UcumVersion version) {

    private static final PrinterService printerService = new Printer();

    public String toStorageKey(FeatureFlags flags) {
        return FeatureFlags.toStorageKey(printerService.print(expression, Printer.PrintType.UCUM_SYNTAX), flags, version);
    }

    public static CanonKey fromStorageKey(String storageKey, UcumVersion ucumVersion) {
        ValKey valKey = FeatureFlags.fromStorageKey(storageKey);
        return new CanonKey(Validator.parseByPassChecks(valKey.expression(), ucumVersion), valKey.flags(), ucumVersion);
    }

    public static CanonKey of(UCUMExpression expression, UcumVersion ucumVersion) {
        return new CanonKey(expression, FeatureFlagsContext.get(), ucumVersion);
    }
}
