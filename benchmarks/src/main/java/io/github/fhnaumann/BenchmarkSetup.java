package io.github.fhnaumann;

import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Felix Naumann
 */
public class BenchmarkSetup {

    public record Data(List<TestCase.ValidateTestCase> validateCases, List<TestCase.CommensurableTestCase> commensurableCases, List<TestCase.ConvertTestCase> convertCases, UcumEssenceService service) {}

    public static Data loadSetup(String ucumateCaching) throws IOException, UcumException {
        TestSuite suite = TestCaseLoader.load();
        List<TestCase.ValidateTestCase> validateCases = suite.validate;
        List<TestCase.CommensurableTestCase> commensurableCases = suite.commensurable;
        List<TestCase.ConvertTestCase> convertCases = suite.convert;

        UcumEssenceService service = new UcumEssenceService(BenchmarkFunctionalXMLTests.class.getResourceAsStream("/ucum-essence.xml"));

        // just to make sure any "accidental" caching in setup loading is removed
        PersistenceRegistry.disableInMemoryCache(true);

        if(ucumateCaching == null) {
            System.out.println("caching not configured!");
            throw new RuntimeException();
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
        return new Data(validateCases, commensurableCases, convertCases, service);
    }
}
