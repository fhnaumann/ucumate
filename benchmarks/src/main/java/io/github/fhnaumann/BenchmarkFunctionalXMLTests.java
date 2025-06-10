package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.UCUMRegistry;
import org.fhir.ucum.Decimal;
import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.fhir.ucum.utils.XmlUtils;
import org.openjdk.jmh.annotations.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public class BenchmarkFunctionalXMLTests {

    private List<Element> validationCases;
    private List<Element> conversionCases;

    private UcumEssenceService service;

    @Param({"disable", "enable", "enableWithPreHeat"})
    public String ucumateCaching;

    @Setup(Level.Iteration)
    public void loadData() throws IOException, ParserConfigurationException, SAXException, UcumException {
        validationCases = new ArrayList<>();
        conversionCases = new ArrayList<>();

        Document doc = XmlUtils.parseDOM(TestCaseLoader.loadGrahamFunctionalTests());
        Element root = doc.getDocumentElement();
        Element section = XmlUtils.getFirstChild(root);

        while (section != null) {
            String type = section.getNodeName();
            List<Element> cases = XmlUtils.getNamedChildren(section, "case");

            switch (type) {
                case "validation" -> validationCases.addAll(cases);
                case "conversion" -> conversionCases.addAll(cases);
            }

            section = XmlUtils.getNextSibling(section);
        }
        service = new UcumEssenceService(BenchmarkFunctionalXMLTests.class.getResourceAsStream("/ucum-essence.xml"));

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

    @Benchmark
    public void benchmarkUcumateValidation() {
        for (Element test : validationCases) {
            UCUMService.validate(test.getAttribute("unit"));
        }
    }

    @Benchmark
    public void benchmarkUcumateConversion() {
        for (Element test : conversionCases) {
            String value = test.getAttribute("value");
            String srcUnit = test.getAttribute("srcUnit");
            String dstUnit = test.getAttribute("dstUnit");
            UCUMService.convert(value, srcUnit, dstUnit);
        }
    }

    @Benchmark
    public void benchmarkUcumJavaValidation() {
        for(Element test : validationCases) {
            service.validate(test.getAttribute("unit"));
        }
    }

    @Benchmark
    public void benchmarkUcumJavaConversion() throws UcumException {
        for(Element test : conversionCases) {
            String value = test.getAttribute("value");
            String srcUnit = test.getAttribute("srcUnit");
            String dstUnit = test.getAttribute("dstUnit");
            service.convert(new Decimal(value), srcUnit, dstUnit);
        }
    }
}
