package io.github.fhnaumann.flattener;

import io.github.fhnaumann.builders.CombineTermBuilder;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Flattener;
import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.fhnaumann.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class FlattenerTest {

    @Test
    public void test() {

        String in = "10/5";
        List<Map. Entry<UCUMExpression.CanonicalUnit, Integer>> map = Flattener.flatten(parse_canonical(in));
        System.out.println(map);
        UCUMExpression.CanonicalTerm test = Flattener.buildFlatProduct(map);
        //Expression.CanonicalTerm inTerm = Main.visitCanonicalTerm(in);
        var inTerm = CombineTermBuilder.builder().left(SoloTermBuilder.builder().withIntegerUnit(10).noExpNoAnnot().asTerm().build()).divideBy().right(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build()).buildCanonical();
        var tmp = Flattener.flattenAndCancel(inTerm);
    }

    @Test
    public void test2() {
        UCUMExpression.Term inTerm = CombineTermBuilder.builder().left(SoloTermBuilder.builder().withoutPrefix(newton).noExpNoAnnot().asTerm().build()).multiplyWith().right(meter_term()).build();
        UCUMExpression.CanonicalTerm canonicalTerm = ((Canonicalizer.Success) new Canonicalizer().canonicalize(inTerm)).canonicalTerm();
        var tmp = Flattener.flattenAndCancel(canonicalTerm);
    }

    @Test
    public void test3() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/s");
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        assertThat(print(flattenAndCancel))
                .isEqualTo("m1.s-1");
    }

    @Test
    public void test4() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s.g2)");
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        assertThat(print(flattenAndCancel))
                .isEqualTo("m1.s-1.g-2");
    }

    @Test
    public void test5() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s/g)");
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        assertThat(print(flattenAndCancel))
                .isEqualTo("m1.s-1.g1");
    }

    @Test
    public void test6() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s/(g/C))");
        System.out.println(print(term));
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        var tmp = Flattener.flattenToProduct(term);
        System.out.println(print(tmp));
        assertThat(print(flattenAndCancel))
                .isEqualTo("C-1.m1.s-1.g1");
    }

    @Test
    public void test_functional_tests_3_126() {

    }
}
