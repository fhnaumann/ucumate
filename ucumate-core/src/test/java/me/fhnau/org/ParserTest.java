package me.fhnau.org;

import me.fhnau.org.funcs.printer.ExpressiveUCUMSyntaxPrinter;
import me.fhnau.org.model.UCUMExpression;
import org.junit.jupiter.api.Test;

import static me.fhnau.org.TestUtil.*;
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
