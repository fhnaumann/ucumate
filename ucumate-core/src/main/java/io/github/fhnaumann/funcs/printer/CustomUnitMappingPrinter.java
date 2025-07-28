package io.github.fhnaumann.funcs.printer;

import io.github.fhnaumann.model.UCUMDefinition;

import java.util.function.Function;

/**
 * @author Felix Naumann
 */
public class CustomUnitMappingPrinter extends Printer {

    private final Function<UCUMDefinition.Concept, String> unitMapping;

    public CustomUnitMappingPrinter(Function<UCUMDefinition.Concept, String> unitMapping) {
        this.unitMapping = unitMapping;
    }

    @Override
    protected String printUCUMDef(UCUMDefinition ucumDefinition) {
        return switch (ucumDefinition) {
            case UCUMDefinition.Concept concept -> unitMapping.apply(concept);
        };
    }
}
