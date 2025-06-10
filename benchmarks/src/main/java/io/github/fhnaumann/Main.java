package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.fhir.ucum.UcumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author Felix Naumann
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, UcumException, ParserConfigurationException, SAXException {
        //org.openjdk.jmh.Main.main(args);
        //logger.debug("TEST");
        Properties properties = new Properties();
        properties.put("ucumate.cache.preheat", true);
        PersistenceRegistry.initCache(properties);
        //UCUMService.validate("/cm[H2O]");
        var obj = new BenchmarkFunctionalJSONTests();
        obj.loadData();
        List<String> diff = obj.aggregateWhereUcumJavaDiffers();
        System.out.println(String.join("\n", diff));
    }

    public void runCode() {

    }
}