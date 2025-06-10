package io.github.fhnaumann.canonicalizer;

import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.Canonicalizer;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.funcs.printer.UCUMSyntaxPrinter;
import io.github.fhnaumann.model.UCUMExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.fhnaumann.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class CanonicalizerTest {

    private Canonicalizer canonicalizer;

    @BeforeEach
    public void setUp() {
        canonicalizer = new Canonicalizer();
    }

    @Test
    public void test() {
        UCUMExpression.Term term = SoloTermBuilder.builder()
                .withoutPrefix(inches)
                .asComponent()
                .withExponent(2)
                .withoutAnnotation()
                .asTerm().build();
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(term);
        assertThat(result)
                .isInstanceOf(Canonicalizer.Success.class)
                .extracting(Canonicalizer.Success.class::cast)
                .satisfies(success -> {
                    System.out.println(success.canonicalTerm());
                });
    }

    @Test
    public void test2() {
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate("g.m.s-2.A-2")).term();
        UCUMExpression.Term term2 = ((Validator.Success) Validator.validate("g.m.C-2")).term();
        Printer p = new UCUMSyntaxPrinter();
        System.out.println(p.print(term1));
        System.out.println(p.print(term2));
        UCUMExpression canon1 = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1)).canonicalTerm();
        UCUMExpression canon2 = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term2)).canonicalTerm();
        System.out.println(p.print(canon1));
        System.out.println(p.print(canon2));
    }

    @Test
    public void test_component_with_exponent_is_canonicalized_into_parts() {
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate("A-2")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1));
        assertThat(new UCUMSyntaxPrinter().print(result.canonicalTerm()))
                .isEqualTo("C-2/s-2");
    }

    @Test
    public void test_component_with_prefix_and_exponent_is_canonicalized_into_parts() {
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate("cA-2")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1));
        assertThat(new UCUMSyntaxPrinter().print(result.canonicalTerm()))
                .isEqualTo("C-2/s-2");
    }

    @Test
    public void test_functional_tests_3_126_1() {
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate("Ohm")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1));
        assertThat(print(result.canonicalTerm()))
                .isEqualTo("C-2.m2.s-1.g1");
    }

    @Test
    public void test_functional_tests_3_126_2() {
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate("Ohm-1")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1));

        assertThat(print(result.canonicalTerm()))
                .isEqualTo("C2.m-2.s1.g-1");
    }

    @Test
    public void test_functional_tests_3_126() {
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate("Ohm")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1));
        System.out.println(new UCUMSyntaxPrinter().print(result.canonicalTerm()));

        UCUMExpression.Term term2 = ((Validator.Success) Validator.validate("Ohm-1")).term();
        Canonicalizer.Success result2 = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term2));
        System.out.println(new UCUMSyntaxPrinter().print(result2.canonicalTerm()));

        UCUMExpression.Term term3 = ((Validator.Success) Validator.validate("S")).term();
        Canonicalizer.Success result3 = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term3));
        System.out.println(new UCUMSyntaxPrinter().print(result3.canonicalTerm()));
    }
}
