package org.example.flattener;

import org.example.Main;
import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.funcs.Flattener;
import org.example.funcs.PrettyPrinter;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.example.TestUtil.*;

public class FlattenerTest {

    @Test
    public void test() {

        String in = "10/5";
        List<Map. Entry<Expression.CanonicalUnit, Integer>> map = Flattener.flatten(Main.visitCanonicalTerm(in));
        System.out.println(map);
        Expression.CanonicalTerm test = Flattener.buildFlatProduct(map);
        System.out.println(PrettyPrinter.defaultPrettyPrinter(test));
        //Expression.CanonicalTerm inTerm = Main.visitCanonicalTerm(in);
        var inTerm = CombineTermBuilder.builder().left(SoloTermBuilder.builder().withIntegerUnit(10).noExpNoAnnot().asTerm().build()).divideBy().right(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build()).buildCanonical();
        var tmp = Flattener.flattenAndCancel(inTerm);
        System.out.println(PrettyPrinter.defaultPrettyPrinter(tmp));
    }

    @Test
    public void test2() {
        Expression.Term inTerm = CombineTermBuilder.builder().left(SoloTermBuilder.builder().withoutPrefix(newton).noExpNoAnnot().asTerm().build()).multiplyWith().right(meter_term()).build();
        Expression.CanonicalTerm canonicalTerm = ((Canonicalizer.Success) new Canonicalizer().canonicalizeNoSpecialUnitAllowed(inTerm)).canonicalTerm();
        var tmp = Flattener.flattenAndCancel(canonicalTerm);
        System.out.println(PrettyPrinter.defaultPrettyPrinter(tmp));
    }
}
