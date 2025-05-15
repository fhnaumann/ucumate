package org.example.dimanalyzer;

import org.example.Main;
import org.example.builders.CombineTermBuilder;
import org.example.funcs.Dimension;
import org.example.funcs.DimensionAnalyzer;
import org.example.model.Expression;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.example.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DimAnalyzerTest {

    @Test
    public void test() {
        //Expression.Term term = CombineTermBuilder.builder().left(meter_term()).divideBy().right(CombineTermBuilder.builder().left(meter_term()).multiplyWith().right(second_term()).build()).build();
        //Expression.Term term = CombineTermBuilder.builder().left(CombineTermBuilder.builder().left(meter_term()).divideBy().right(second_term()).build()).divideBy().right(gram_term()).build();
        Map<Dimension, Integer> result = DimensionAnalyzer.analyze(Main.visitCanonicalTerm("m/(m.s)"));
        System.out.println(result);
    }
}
