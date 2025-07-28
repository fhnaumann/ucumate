package io.github.fhnaumann.dimanalyzer;

import io.github.fhnaumann.funcs.*;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.fhnaumann.TestUtil.parse_canonical;
import static org.assertj.core.api.Assertions.assertThat;

public class DimAnalyzerTest {

    @Test
    public void test() {
        //Expression.Term term = CombineTermBuilder.builder().left(meter_term()).divideBy().right(CombineTermBuilder.builder().left(meter_term()).multiplyWith().right(second_term()).build()).build();
        //Expression.Term term = CombineTermBuilder.builder().left(CombineTermBuilder.builder().left(meter_term()).divideBy().right(second_term()).build()).divideBy().right(gram_term()).build();
        Map<DimensionType, Integer> result = DimensionAnalyzer.analyze(parse_canonical("s/4/m"));
        Map<DimensionType, Integer> result2 = DimensionAnalyzer.analyze(parse_canonical("s/m"));
    }

    @Test
    public void test_functional_tests_3_126() {
        UCUMExpression.Term from = ((Validator.Success)new Validator().validate("S")).term();
        //Expression.Term to = ((Validator.Success)Validator.validate("g-1.m-2.C2.s")).term();
        //Expression.Term to = ((Validator.Success)Validator.validate("m/g")).term();

        UCUMExpression.CanonicalTerm canonicalFrom = ((CanonicalizerService.Success)new Canonicalizer(new Printer(), new Validator()).canonicalize(from)).canonicalTerm();
        //Expression.CanonicalTerm canonicalTo = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(to)).canonicalTerm();
        var map1 = DimensionAnalyzer.analyze(canonicalFrom);
        //var map2 = DimensionAnalyzer.analyze(canonicalTo);
        assertThat(map1)
                .contains(Map.entry(DimensionType.ELECTRIC_CHARGE, 2))
                .contains(Map.entry(DimensionType.MASS, -1))
                .contains(Map.entry(DimensionType.LENGTH, -2))
                .contains(Map.entry(DimensionType.TIME, 1));
        System.out.println(map1);
        //System.out.println(map2);
        //System.out.println(Flattener.flatten(canonicalTo));
        /*
        Converter.ConversionResult result = new Converter().convert(new Converter.Conversion(PreciseDecimal.ONE, from), to);
        assertThat(result)
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast);

         */
    }


}
