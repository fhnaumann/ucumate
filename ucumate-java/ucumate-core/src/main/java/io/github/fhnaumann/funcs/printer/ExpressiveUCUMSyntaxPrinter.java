package io.github.fhnaumann.funcs.printer;

import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UCUMDefinition;

public class ExpressiveUCUMSyntaxPrinter extends Printer {

    public ExpressiveUCUMSyntaxPrinter() {
        super();
    }

    public ExpressiveUCUMSyntaxPrinter(ValidatorService validatorService) {
        super(validatorService);
    }

    @Override
    protected String printUCUMDef(UCUMDefinition ucumDefinition) {
        return switch (ucumDefinition) {
            case UCUMDefinition.Concept concept -> "%s (%s)".formatted(concept.printSymbol(), concept.names().stream().findFirst().orElseThrow());
        };
    }
}
