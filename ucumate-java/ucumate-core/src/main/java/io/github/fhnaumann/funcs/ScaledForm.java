package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;

/**
 * The magnitude and canonical term of a canonicalization result. Terms with arbitrary units don't have a canonical form,
 * but they can still be compared to scalar multiples of the same arbitrary unit(s), so their scaled form is used as well.
 * @param magnitude The magnitude of the canonicalization.
 * @param canonicalTerm The canonical term (with arbitrary units kept as opaque atoms).
 * @param hasArbitraryUnits True if the canonical term contains arbitrary units, false otherwise.
 */
record ScaledForm(PreciseDecimal magnitude, UCUMExpression.CanonicalTerm canonicalTerm, boolean hasArbitraryUnits) {

    /**
     * @param result The canonicalization result.
     * @return The scaled form or null if the canonicalization failed for any other reason than arbitrary units.
     */
    static ScaledForm of(CanonicalizerService.CanonicalizationResult result) {
        return switch (result) {
            case CanonicalizerService.Success success -> new ScaledForm(success.magnitude(), success.canonicalTerm(), false);
            case CanonicalizerService.TermHasArbitraryUnit arbitrary -> new ScaledForm(arbitrary.magnitude(), arbitrary.canonicalTerm(), true);
            case CanonicalizerService.FailedCanonicalization failedCanonicalization -> null;
        };
    }
}
