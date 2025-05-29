package me.fhnau.org.funcs.printer;

import me.fhnau.org.model.UCUMDefinition;

public class ExpressiveUCUMSyntaxPrinter extends Printer {

    @Override
    protected String printUCUMDef(UCUMDefinition ucumDefinition) {
        return switch (ucumDefinition) {
            case UCUMDefinition.Concept concept -> "%s (%s)".formatted(concept.printSymbol(), concept.names().stream().findFirst().orElseThrow());
        };
    }
}
