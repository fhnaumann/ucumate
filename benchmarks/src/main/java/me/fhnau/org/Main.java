package me.fhnau.org;

import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.persistence.PersistenceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * @author Felix Naumann
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        //org.openjdk.jmh.Main.main(args);
        //logger.debug("TEST");
        Properties properties = new Properties();
        properties.put("ucumate.cache.preheat", true);
        PersistenceRegistry.initCache(properties);
        UCUMService.validate("/cm[H2O]");
    }

    public void runCode() {

    }
}