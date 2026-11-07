package io.github.fhnaumann.validation;

import io.github.fhnaumann.BenchmarkSetup;
import io.github.fhnaumann.TestCase;
import org.fhir.ucum.UcumException;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Felix Naumann
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(3)
@State(Scope.Benchmark)
public class BenchmarkValidationJSON {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkValidationJSON.class);

    private BenchmarkSetup.Data data;

    @Param({"disable", "enable"})
    public String ucumateCaching;

    private Random random;
    private int[] randomIndices;

    @Setup(Level.Trial)
    public void loadData() throws IOException, ParserConfigurationException, SAXException, UcumException {
        data = BenchmarkSetup.loadSetup(ucumateCaching);
        //logger.warn("Cache size after loading data: " + PersistenceRegistry.getInstance().getAllValidated().size());
        random = new Random(42);  // Fixed seed for reproducibility

        // Pre-generate random indices to avoid RNG overhead in benchmark
        int size = data.validateCases().size();
        randomIndices = new int[size];
        for (int i = 0; i < size; i++) {
            randomIndices[i] = random.nextInt(size);
        }
    }


    @Benchmark
    public void benchmarkUcumJavaValidation() {
//        for (TestCase.ValidateTestCase testCase : data.validateCases()) {
//            data.service().validate(testCase.inputExpression());
//        }
        for (int idx : randomIndices) {
            data.service().validate(data.validateCases().get(idx).inputExpression());
        }
    }

    @Benchmark
    public void benchmarkUcumateValidation() {
        for (int idx : randomIndices) {
            data.ucumateService().validateToBool(data.validateCases().get(idx).inputExpression());
        }
        //logger.warn("Cache size: " + PersistenceRegistry.getInstance().getAllValidated().size());
//        for (TestCase.ValidateTestCase testCase : data.validateCases()) {
//            data.ucumateService().validateToBool(testCase.inputExpression());
//            //logger.warn("After Cache size: " + PersistenceRegistry.getInstance().getAllValidated().size());
//        }
    }

}
