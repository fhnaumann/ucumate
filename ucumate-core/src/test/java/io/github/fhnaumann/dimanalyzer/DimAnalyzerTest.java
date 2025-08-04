package io.github.fhnaumann.dimanalyzer;

import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.fhnaumann.TestUtil.parse_canonical;
import static org.assertj.core.api.Assertions.assertThat;

public class DimAnalyzerTest {

    @Test
    public void test_functional_tests_3_126() {
        UCUMExpression.Term from = ((Validator.Success)new Validator().validate("S")).term();
        UCUMExpression.CanonicalTerm canonicalFrom = ((CanonicalizerService.Success)new Canonicalizer(new Printer(), new Validator()).canonicalize(from)).canonicalTerm();
        var map1 = DimensionAnalyzer.analyze(canonicalFrom);
        assertThat(map1)
                .contains(Map.entry(DimensionType.ELECTRIC_CHARGE, 2))
                .contains(Map.entry(DimensionType.MASS, -1))
                .contains(Map.entry(DimensionType.LENGTH, -2))
                .contains(Map.entry(DimensionType.TIME, 1));
    }


}
