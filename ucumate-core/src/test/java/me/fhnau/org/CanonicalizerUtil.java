package me.fhnau.org;

import me.fhnau.org.funcs.Canonicalizer;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.util.PreciseDecimal;

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
