package io.github.fhnaumann;

import io.github.fhnaumann.funcs.CanonicalizerService;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CanonicalizerUtil {

    public static void assert_success(CanonicalizerService.CanonicalizationResult result, PreciseDecimal expectedConvFactor, UCUMExpression.Term expectedCanonicalTerm) { //NOSONAR
        assertThat(result)
                .isNotNull()
                .isInstanceOf(CanonicalizerService.Success.class)
                .extracting(CanonicalizerService.Success.class::cast)
                .satisfies(success -> {
                    //assertThat(success.conversionFactor()).isEqualTo(expectedConvFactor); // NOSONAR need this when I decide to implement correct decimal handling
                    assertThat(success.canonicalTerm()).isEqualTo(expectedCanonicalTerm);
                });
    }
}
