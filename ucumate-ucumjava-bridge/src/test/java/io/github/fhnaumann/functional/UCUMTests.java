package io.github.fhnaumann.functional;

import io.github.fhnaumann.TestCase;
import io.github.fhnaumann.TestCaseLoader;
import io.github.fhnaumann.TestSuite;
import io.github.fhnaumann.UcumateToUcumJavaTestBase;
import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.funcs.RelationChecker;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("functional-tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UCUMTests extends UcumateToUcumJavaTestBase {

    private static TestSuite testSuite;

    @BeforeAll
    public void initalSetup() throws IOException {
        testSuite = TestCaseLoader.load();
    }

    public static Stream<TestCase.ValidateTestCase> validateTestCases() {
        return testSuite.validate.stream();
    }

    public static Stream<TestCase.CommensurableTestCase> commensurableTestCases() {
        return testSuite.commensurable.stream();
    }

    public static Stream<TestCase.ConvertTestCase> convertTestCases() {
        return testSuite.convert.stream();
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("validateTestCases")
    public void testValidation(TestCase.ValidateTestCase testCase) {
        boolean oldExpected = oldService.validate(testCase.inputExpression()) == null;
        boolean actual = newService.validate(testCase.inputExpression()) == null;
        assumeTrue(oldExpected == testCase.valid(), "%s: JSON Tests says %s but Ucum-java says %s".formatted(testCase.id(), testCase.valid(), oldExpected));
        assumeTrue(actual == testCase.valid(), "%s: JSON Tests says %s but bridge says %s".formatted(testCase.id(), testCase.valid(), actual));

        //assertEquals(testCase.valid(), actual, "%s: Expected %s but got %s, reason: %s".formatted(testCase.id(), testCase.valid(), actual, testCase.reason()));

    }

    @ParameterizedTest(name="{0}")
    @MethodSource("commensurableTestCases")
    public void testCommensurability(TestCase.CommensurableTestCase testCase) {
        RelationChecker.RelationResult result = UCUMService.checkCommensurable(TestUtil.parse(testCase.expr1()), TestUtil.parse(testCase.expr2()), false);
        assertEquals(testCase.commensurable(), result instanceof RelationChecker.IsCommensurable, testCase.toString());
    }

    @ParameterizedTest(name="{0}")
    @MethodSource("convertTestCases")
    public void testConversion(TestCase.ConvertTestCase testCase) {
        PreciseDecimal fromFactor = new PreciseDecimal(testCase.conversionFactor());
        PreciseDecimal toFactor = new PreciseDecimal(testCase.resultingConversionFactor());
        UCUMExpression.Term from = TestUtil.parse(testCase.from());
        UCUMExpression.Term to = TestUtil.parse(testCase.to());
        Converter.ConversionResult result = UCUMService.convert(fromFactor, from, to, testCase.substanceMolarMassCoeff() != null ? new PreciseDecimal(testCase.substanceMolarMassCoeff()) : null);
        if(testCase.valid()) {
            Assertions.assertThat(result)
                .withFailMessage("%s: Unexpected validation error while testing the conversion: %s".formatted(testCase.id(), result))
                .isInstanceOf(Converter.Success.class)
                .extracting(Converter.Success.class::cast)
                .extracting(Converter.Success::conversionFactor)
                .satisfies(pd -> {
                    TestUtil.skipIfRoundingProblem(toFactor.toString(), pd);
                    Assertions.assertThat(pd)
                            .asString()
                            .withFailMessage(() -> "%s: Expected resulting conversion factor of %s but got %s".formatted(testCase.id(), toFactor, pd))
                            .startsWith(toFactor.toString());
                });
        }
        else {
            Assertions.assertThat(result)
                .isInstanceOf(Converter.FailedConversion.class);
        }

    }


    @AfterAll
    public static void tearDown() {
        //System.out.println(Validator.cache.stats());
        //System.out.println(Canonicalizer.cache.stats());
        // System.out.println(GraphLayout.parseInstance(Canonicalizer.cache).toFootprint());
    }

}
