package me.fhnau.org;

import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.persistence.PersistenceRegistry;
import org.fhir.ucum.UcumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

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