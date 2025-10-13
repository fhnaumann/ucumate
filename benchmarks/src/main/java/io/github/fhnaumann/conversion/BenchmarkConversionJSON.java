package io.github.fhnaumann.conversion;

import io.github.fhnaumann.BenchmarkSetup;
import io.github.fhnaumann.TestCase;
import org.fhir.ucum.Decimal;
import org.fhir.ucum.UcumException;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Felix Naumann
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(3)
@State(Scope.Benchmark)
public class BenchmarkConversionJSON {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkConversionJSON.class); //NOSONAR needed later

    private BenchmarkSetup.Data data;

    @Param({"disable", "enable", "enableWithPreHeat"})
    public String ucumateCaching;

    private Random random;
    private int[] randomIndices;

    private Map<String, Decimal> conversionFactorMap = new HashMap<>();

    private List<TestCase.ConvertTestCase> compatibleCases;

    @Setup(Level.Trial)
    public void loadData() throws IOException, ParserConfigurationException, SAXException, UcumException {
        data = BenchmarkSetup.loadSetup(ucumateCaching);

        random = new Random(42);  // Fixed seed for reproducibility

        compatibleCases = data.convertCases().stream()
                .filter(tc -> {
                    try {
                        data.service().convert(new Decimal(tc.conversionFactor()),
                                tc.from(), tc.to());
                        return true;
                    } catch (UcumException e) {
                        return false;
                    }
                })
                .toList();

        // Pre-generate random indices to avoid RNG overhead in benchmark
        int size = compatibleCases.size();
        randomIndices = new int[size];
        for (int i = 0; i < size; i++) {
            randomIndices[i] = random.nextInt(size);
        }

        compatibleCases.forEach(convertTestCase -> {
            try {
                conversionFactorMap.put(convertTestCase.conversionFactor(), new Decimal(convertTestCase.conversionFactor()));
            } catch (UcumException e) {
                throw new RuntimeException(e);
            }
        });


    }


    @Benchmark
    public void benchmarkucumJavaConversion() throws UcumException {
        for (int idx : randomIndices) {
            TestCase.ConvertTestCase tCase = compatibleCases.get(idx);
            data.service().convert(conversionFactorMap.get(tCase.conversionFactor()), tCase.from(), tCase.to());
        }
//        for (TestCase.ConvertTestCase testCase : data.convertCases()) {
//            try {
//                data.service().convert(new Decimal(testCase.conversionFactor()), testCase.from(), testCase.to());
//            } catch (UcumException ignored) {
//
//            }
//        }
    }

    @Benchmark
    public void benchmarkUcumateConversion() {

//        for(int idx : randomIndices) {
//            TestCase.ConvertTestCase tCase = compatibleCases.get(idx);
//            data.ucumateService().convert(tCase.conversionFactor(), tCase.from(), tCase.to());
//        }

        //logger.warn("Cache size: " + PersistenceRegistry.getInstance().getAllCanonical().size());
        for (TestCase.ConvertTestCase testCase : compatibleCases) {
            data.ucumateService().convert(testCase.conversionFactor(), testCase.from(), testCase.to());
            //logger.warn("After Cache size: " + PersistenceRegistry.getInstance().getAllCanonical().size());
        }
    }
}
