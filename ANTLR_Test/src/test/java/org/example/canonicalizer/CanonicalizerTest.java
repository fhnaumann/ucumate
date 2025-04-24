package org.example.canonicalizer;

import org.example.builders.SoloTermBuilder;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtil.*;

public class CanonicalizerTest {

    private Canonicalizer canonicalizer;

    @BeforeEach
    public void setUp() {
        canonicalizer = new Canonicalizer();
    }

    @Test
    public void test() {
        Expression.Term term = SoloTermBuilder.builder()
                .withoutPrefix(inches)
                .asComponent()
                .withExponent(2)
                .withoutAnnotation()
                .asTerm().build();
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(term, new Canonicalizer.SpecialUnitConversionContext(
                PreciseDecimal.ONE, Canonicalizer.SpecialUnitApplicationDirection.NO_SPECIAL_INVOLVED));
        assertThat(result)
                .isInstanceOf(Canonicalizer.Success.class)
                .extracting(Canonicalizer.Success.class::cast)
                .satisfies(success -> {
                    System.out.println(success.conversionFactor());
                    System.out.println(success.canonicalTerm());
                });
    }
}
