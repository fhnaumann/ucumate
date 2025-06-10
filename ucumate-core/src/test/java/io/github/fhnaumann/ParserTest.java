package io.github.fhnaumann;

import io.github.fhnaumann.funcs.printer.ExpressiveUCUMSyntaxPrinter;
import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import static io.github.fhnaumann.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {


    @Test
    public void test_ambiguity_when_parsing_prefix_or_unit() {
        UCUMExpression.Term term = parse_canonical("cd");
        assertThat(term)
                .withFailMessage("Expected cd (candela) but got %s", new ExpressiveUCUMSyntaxPrinter().print(term))
                .isEqualTo(single(getUCUMUnit("cd")));
    }
}
