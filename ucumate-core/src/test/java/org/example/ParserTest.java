package org.example;

import org.example.builders.SoloTermBuilder;
import org.example.funcs.printer.ExpressiveUCUMSyntaxPrinter;
import org.example.funcs.printer.UCUMSyntaxPrinter;
import org.example.model.Expression;
import org.example.model.ExpressionBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import static org.example.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {


    @Test
    public void test_ambiguity_when_parsing_prefix_or_unit() {
        Expression.Term term = parse_canonical("cd");
        assertThat(term)
                .withFailMessage("Expected cd (candela) but got %s", new ExpressiveUCUMSyntaxPrinter().print(term))
                .isEqualTo(single(getUCUMUnit("cd")));
    }
}
