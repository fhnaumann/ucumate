package io.github.fhnaumann.validation;

import io.github.fhnaumann.BenchmarkSetup;
import io.github.fhnaumann.TestCase;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.fhir.ucum.UcumException;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Felix Naumann
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(0)
@State(Scope.Thread)
public class BenchmarkValidationJSON {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkValidationJSON.class);

    private BenchmarkSetup.Data data;

    @Param({"disable", "enable", "enableWithPreHeat"})
    public String ucumateCaching;

    @Setup(Level.Iteration)
    public void loadData() throws IOException, ParserConfigurationException, SAXException, UcumException {
        data = BenchmarkSetup.loadSetup(ucumateCaching);
        //logger.warn("Cache size after loading data: " + PersistenceRegistry.getInstance().getAllValidated().size());
    }


    @Benchmark
    public void benchmarkUcumJavaValidation() {
        for (TestCase.ValidateTestCase testCase : data.validateCases()) {
            data.service().validate(testCase.inputExpression());
        }
    }

    @Benchmark
    public void benchmarkUcumateValidation() {
        //logger.warn("Cache size: " + PersistenceRegistry.getInstance().getAllValidated().size());
        for (TestCase.ValidateTestCase testCase : data.validateCases()) {
            UCUMService.validateToBool(testCase.inputExpression());
            //logger.warn("After Cache size: " + PersistenceRegistry.getInstance().getAllValidated().size());
        }
    }

}
