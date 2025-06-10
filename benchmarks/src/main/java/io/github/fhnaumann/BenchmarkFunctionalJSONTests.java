package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.fhir.ucum.Decimal;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.openjdk.jmh.annotations.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Felix Naumann
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 2, time = 1)
@Fork(2)
@State(Scope.Thread)
public class BenchmarkFunctionalJSONTests {

    private List<TestCase.ValidateTestCase> validateCases;
    private List<TestCase.CommensurableTestCase> commensurableCases;
    private List<TestCase.ConvertTestCase> convertCases;

    private UcumEssenceService service;

    @Param({"disable", "enable", "enableWithPreHeat"})
    public String ucumateCaching;

    @Setup(Level.Iteration)
    public void loadData() throws IOException, ParserConfigurationException, SAXException, UcumException {
        TestSuite suite = TestCaseLoader.load();
        validateCases = suite.validate;
        commensurableCases = suite.commensurable;
        convertCases = suite.convert;

        service = new UcumEssenceService(BenchmarkFunctionalXMLTests.class.getResourceAsStream("/ucum-essence.xml"));

        if(ucumateCaching == null) {
            System.out.println("caching not configured!");
            return;
        }

        if(ucumateCaching.equals("disable")) {
            PersistenceRegistry.disableInMemoryCache(true);
        }
        else {
            Properties properties = new Properties();
            properties.put("ucumate.cache.enable", true);
            properties.put("ucumate.cache.preheat", ucumateCaching.equals("enableWithPreHeat"));
            PersistenceRegistry.initCache(properties);
        }
    }

    public List<String> aggregateWhereUcumJavaDiffers() {
        List<String> diff = new ArrayList<>();
        for (TestCase.ValidateTestCase testCase : validateCases) {
            boolean ucumJavaValid = service.validate(testCase.inputExpression()) == null;
            boolean ucumateValid = UCUMService.validateToBool(testCase.inputExpression());
            if(ucumJavaValid != ucumateValid) {
                diff.add("id %s: Input %s: ucum-java says %s but ucumate says %s.".formatted(testCase.id(), testCase.inputExpression(), ucumJavaValid, ucumateValid));
            }
        }
        return diff;
    }

    @Benchmark
    public void benchmarkUcumJavaValidation() {
        for (TestCase.ValidateTestCase testCase : validateCases) {
            service.validate(testCase.inputExpression());
        }
    }

    @Benchmark
    public void benchmarkUcumJavaCommensurability() {
        for (TestCase.CommensurableTestCase testCase : commensurableCases) {
            try {
                service.isComparable(testCase.expr1(), testCase.expr2());
            } catch (UcumException ignored) {

            }
        }
    }

    @Benchmark
    public void benchmarkucumJavaConversion() throws UcumException {
        for (TestCase.ConvertTestCase testCase : convertCases) {
            try {
                service.convert(new Decimal(testCase.conversionFactor()), testCase.from(), testCase.to());
            } catch (UcumException ignored) {

            }
        }
    }

    @Benchmark
    public void benchmarkUcumateValidation() {
        for (TestCase.ValidateTestCase testCase : validateCases) {
            UCUMService.validateToBool(testCase.inputExpression());
        }
    }

    @Benchmark
    public void benchmarkUcumateCommensurability() {
        for (TestCase.CommensurableTestCase testCase : commensurableCases) {
            UCUMService.checkCommensurable(
                    testCase.expr1(),
                    testCase.expr2()
            );
        }
    }

    @Benchmark
    public void benchmarkUcumateConversion() {
        for (TestCase.ConvertTestCase testCase : convertCases) {
            UCUMService.convert(testCase.conversionFactor(), testCase.from(), testCase.to());
        }
    }

}
