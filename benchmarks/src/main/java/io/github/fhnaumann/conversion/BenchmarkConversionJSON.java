package io.github.fhnaumann.conversion;

import io.github.fhnaumann.BenchmarkSetup;
import io.github.fhnaumann.TestCase;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.fhir.ucum.Decimal;
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
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@Fork(1)
@State(Scope.Thread)
public class BenchmarkConversionJSON {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkConversionJSON.class);

    private BenchmarkSetup.Data data;

    @Param({"disable", "enable", "enableWithPreHeat"})
    public String ucumateCaching;

    @Setup(Level.Iteration)
    public void loadData() throws IOException, ParserConfigurationException, SAXException, UcumException {
        data = BenchmarkSetup.loadSetup(ucumateCaching);
    }


    @Benchmark
    public void benchmarkucumJavaConversion() throws UcumException {
        for (TestCase.ConvertTestCase testCase : data.convertCases()) {
            try {
                data.service().convert(new Decimal(testCase.conversionFactor()), testCase.from(), testCase.to());
            } catch (UcumException ignored) {

            }
        }
    }

    @Benchmark
    public void benchmarkUcumateValidation() {
        // logger.warn("Cache size: " + PersistenceRegistry.getInstance().getAllCanonical().size());
        for (TestCase.ConvertTestCase testCase : data.convertCases()) {
            UCUMService.convert(testCase.conversionFactor(), testCase.from(), testCase.to());
            //logger.warn("After Cache size: " + PersistenceRegistry.getInstance().getAllValidated().size());
        }
    }
}
