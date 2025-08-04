package io.github.fhnaumann.flattener;

import io.github.fhnaumann.funcs.Flattener;
import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import static io.github.fhnaumann.TestUtil.parse_canonical;
import static io.github.fhnaumann.TestUtil.print;
import static org.assertj.core.api.Assertions.assertThat;

public class FlattenerTest {

    @Test
    public void test3() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/s");
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        assertThat(print(flattenAndCancel))
                .isEqualTo("m.s-1");
    }

    @Test
    public void test4() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s.g2)");
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        assertThat(print(flattenAndCancel))
                .isEqualTo("m.s-1.g-2");
    }

    @Test
    public void test5() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s/g)");
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        assertThat(print(flattenAndCancel))
                .isEqualTo("m.s-1.g");
    }

    @Test
    public void test6() {
        UCUMExpression.CanonicalTerm term = parse_canonical("m/(s/(g/C))");
        System.out.println(print(term));
        UCUMExpression.CanonicalTerm flattenAndCancel = Flattener.flattenAndCancel(term);
        var tmp = Flattener.flattenToProduct(term);
        System.out.println(print(tmp));
        assertThat(print(flattenAndCancel))
                .isEqualTo("m.C-1.s-1.g");
    }
}
