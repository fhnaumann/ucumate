package io.github.fhnaumann;

import io.github.fhnaumann.configuration.CacheConfiguration;
import io.github.fhnaumann.funcs.UCUMService;
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

    public record Data(List<TestCase.ValidateTestCase> validateCases, List<TestCase.CommensurableTestCase> commensurableCases, List<TestCase.ConvertTestCase> convertCases, UcumEssenceService service, UCUMService ucumateService) {}

    public static Data loadSetup(String ucumateCaching) throws IOException, UcumException {
        TestSuite suite = TestCaseLoader.load();
        List<TestCase.ValidateTestCase> validateCases = suite.validate;
        List<TestCase.CommensurableTestCase> commensurableCases = suite.commensurable;
        List<TestCase.ConvertTestCase> convertCases = suite.convert;

        System.setProperty("ucumate.cache.preheat", "false");

        if(ucumateCaching.equals("disable")) {
            System.setProperty("ucumate.cache.enable", "false");
        }
        if(ucumateCaching.equals("enable")) {
            System.setProperty("ucumate.cache.enable", "true");
        }

        UcumEssenceService service = new UcumEssenceService(BenchmarkSetup.class.getResourceAsStream("/ucum-essence.xml"));
        UCUMService ucumateService = new UCUMService();
        return new Data(validateCases, commensurableCases, convertCases, service, ucumateService);
    }
}
