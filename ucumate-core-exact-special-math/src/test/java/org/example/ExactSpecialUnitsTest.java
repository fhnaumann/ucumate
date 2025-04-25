package org.example;

import org.example.builders.SoloTermBuilder;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.example.util.PreciseDecimal.ONE;

public class ExactSpecialUnitsTest {

    private Canonicalizer canonicalizer;
    private static UCUMRegistry registry = UCUMRegistry.getInstance();

    @BeforeEach
    public void setUp() {
        canonicalizer = new Canonicalizer();
    }

    @Test
    public void canonicalize_cf1_Cel() {
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(percent_slope_term(), new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.FROM));
        System.out.println(result);
        //assert_success(result, pd("274.1500"), kelvin_term());
    }

    static Expression.Term kelvin_term() {
        return SoloTermBuilder.builder().withoutPrefix(registry.getUCUMUnit("K").get()).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term celsius_term() {
        return SoloTermBuilder.builder().withoutPrefix(registry.getUCUMUnit("Cel").get()).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term percent_slope_term() {
        return SoloTermBuilder.builder().withoutPrefix(registry.getUCUMUnit("%[slope]").get()).noExpNoAnnot().asTerm().build();
    }

    private static PreciseDecimal pd(String s) {
        return new PreciseDecimal(s);
    }


}
