package io.github.fhnaumann;

import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CanonicalizerUtil {

    public static void assert_success(Canonicalizer.CanonicalizationResult result, PreciseDecimal expectedConvFactor, UCUMExpression.Term expectedCanonicalTerm) {
        assertThat(result)
                .isNotNull()
                .isInstanceOf(Canonicalizer.Success.class)
                .extracting(Canonicalizer.Success.class::cast)
                .satisfies(success -> {
                    //assertThat(success.conversionFactor()).isEqualTo(expectedConvFactor);
                    assertThat(success.canonicalTerm()).isEqualTo(expectedCanonicalTerm);
                });
    }
}
