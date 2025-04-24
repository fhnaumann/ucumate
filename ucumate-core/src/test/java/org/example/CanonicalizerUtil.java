package org.example;

import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtil.*;

public class CanonicalizerUtil {

    public static void assert_success(Canonicalizer.CanonicalizationResult result, PreciseDecimal expectedConvFactor, Expression.Term expectedCanonicalTerm) {
        assertThat(result)
                .isNotNull()
                .isInstanceOf(Canonicalizer.Success.class)
                .extracting(Canonicalizer.Success.class::cast)
                .satisfies(success -> {
                    assertThat(success.conversionFactor()).isEqualTo(expectedConvFactor);
                    assertThat(success.canonicalTerm()).isEqualTo(expectedCanonicalTerm);
                });
    }
}
