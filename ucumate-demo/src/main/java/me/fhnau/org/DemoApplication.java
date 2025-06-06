package me.fhnau.org;

import me.fhnau.org.persistence.PersistenceRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;
import java.util.Properties;

/**
 * @author Felix Naumann
 */
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        //Properties props = new Properties();
        //props.put("ucumate.cache.preheat", true);
        //PersistenceRegistry.initCache(props);
        SpringApplication.run(DemoApplication.class, args);
    }
}
