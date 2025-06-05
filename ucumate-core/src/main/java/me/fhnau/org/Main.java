package me.fhnau.org;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.funcs.Converter;
import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.funcs.Validator;
import me.fhnau.org.funcs.printer.Printer;
import me.fhnau.org.persistence.PersistenceRegistry;

import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        //PersistenceRegistry.register("cache-canon", new InMemoryCanonicalizePersistenceProvider());
        UCUMService.canonicalize("S");
        System.out.println(UCUMService.canonicalize("s+2"));

        Validator.ValidationResult valResult = UCUMService.validate("cm.s");
        String print = switch(valResult) {
            case Validator.Failure failure -> "Invalid input";
            case Validator.Success success -> UCUMService.print(success.term(), Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX);
        };
        System.out.println(print); //

        boolean valid = UCUMService.validateToBool("cm.s");
        System.out.println(valid);

        System.out.println(UCUMService.print("cm", Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX));

        for(Printer.PrintType type : Printer.PrintType.values()) {
            System.out.println(UCUMService.print("cm/(s.g2)", type));
        }

        Canonicalizer.CanonicalizationResult canonResult = UCUMService.canonicalize("[in_i]");
        String print2 = switch (canonResult) {
            case Canonicalizer.FailedCanonicalization failedCanonicalization -> "Invalid input";
            case Canonicalizer.Success success -> String.format("Conversion Factor: %s, Canonical Form: %s".formatted(success.magnitude(), UCUMService.print(success.canonicalTerm())));
        };
        System.out.println(print2);

        Converter.ConversionResult convResult = UCUMService.convert("[ft_i]", "[in_i]");
        String print3 = switch (convResult) {
            case Converter.FailedConversion failedConversion -> "Conversion failed";
            case Converter.Success success -> "1 [ft_i] is %s [in_i]".formatted(success.conversionFactor());
        };
        System.out.println(print3); // 1 [ft_i] is 12 [in_i]

        UCUMService.validate("cm");
    }
}
