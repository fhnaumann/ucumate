package org.example.canonicalizer;

import org.example.builders.SoloTermBuilder;
import org.example.funcs.Validator;
import org.example.funcs.printer.Printer;
import org.example.funcs.printer.UCUMSyntaxPrinter;
import org.example.model.Canonicalizer;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtil.*;

public class CanonicalizerTest {

    private Canonicalizer canonicalizer;

    @BeforeEach
    public void setUp() {
        canonicalizer = new Canonicalizer();
    }

    @Test
    public void test() {
        Expression.Term term = SoloTermBuilder.builder()
                .withoutPrefix(inches)
                .asComponent()
                .withExponent(2)
                .withoutAnnotation()
                .asTerm().build();
        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize(term, new Canonicalizer.SpecialUnitConversionContext(
                PreciseDecimal.ONE, Canonicalizer.SpecialUnitApplicationDirection.NO_SPECIAL_INVOLVED));
        assertThat(result)
                .isInstanceOf(Canonicalizer.Success.class)
                .extracting(Canonicalizer.Success.class::cast)
                .satisfies(success -> {
                    System.out.println(success.conversionFactor());
                    System.out.println(success.canonicalTerm());
                });
    }

    @Test
    public void test2() {
        Expression.Term term1 = ((Validator.Success) Validator.validate("g.m.s-2.A-2")).term();
        Expression.Term term2 = ((Validator.Success) Validator.validate("g.m.C-2")).term();
        Printer p = new UCUMSyntaxPrinter();
        System.out.println(p.print(term1));
        System.out.println(p.print(term2));
        Expression canon1 = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term1)).canonicalTerm();
        Expression canon2 = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term2)).canonicalTerm();
        System.out.println(p.print(canon1));
        System.out.println(p.print(canon2));
    }

    @Test
    public void test_component_with_exponent_is_canonicalized_into_parts() {
        Expression.Term term1 = ((Validator.Success) Validator.validate("A-2")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term1));
        assertThat(new UCUMSyntaxPrinter().print(result.canonicalTerm()))
                .isEqualTo("C-2/s-2");
    }

    @Test
    public void test_component_with_prefix_and_exponent_is_canonicalized_into_parts() {
        Expression.Term term1 = ((Validator.Success) Validator.validate("cA-2")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term1));
        assertThat(new UCUMSyntaxPrinter().print(result.canonicalTerm()))
                .isEqualTo("C-2/s-2");
        assertThat(result.conversionFactor().toString()).isEqualTo("10000");
    }

    @Test
    public void test_functional_tests_3_126_1() {
        Expression.Term term1 = ((Validator.Success) Validator.validate("Ohm")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term1));
        assertThat(print(result.canonicalTerm()))
                .isEqualTo("C-2.m2.s-1.g1");
    }

    @Test
    public void test_functional_tests_3_126_2() {
        Expression.Term term1 = ((Validator.Success) Validator.validate("Ohm-1")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalize(term1, new Canonicalizer.SpecialUnitConversionContext(ONE, Canonicalizer.SpecialUnitApplicationDirection.NO_SPECIAL_INVOLVED), true, true));

        assertThat(print(result.canonicalTerm()))
                .isEqualTo("C2.m-2.s1.g-1");
    }

    @Test
    public void test_functional_tests_3_126() {
        Expression.Term term1 = ((Validator.Success) Validator.validate("Ohm")).term();
        Canonicalizer.Success result = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term1));
        System.out.println(new UCUMSyntaxPrinter().print(result.canonicalTerm()));

        Expression.Term term2 = ((Validator.Success) Validator.validate("Ohm-1")).term();
        Canonicalizer.Success result2 = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term2));
        System.out.println(new UCUMSyntaxPrinter().print(result2.canonicalTerm()));

        Expression.Term term3 = ((Validator.Success) Validator.validate("S")).term();
        Canonicalizer.Success result3 = ((Canonicalizer.Success)new Canonicalizer().canonicalizeNoSpecialUnitAllowed(term3));
        System.out.println(new UCUMSyntaxPrinter().print(result3.canonicalTerm()));
    }
}
