package me.fhnau.org;

import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.persistence.PersistenceProvider;
import me.fhnau.org.persistence.PersistenceRegistry;
import me.fhnau.org.util.UCUMRegistry;

import java.util.Properties;

/**
 * @author Felix Naumann
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        Properties props = new Properties();
        props.put("ucumate.cache.enable", false);
        PersistenceRegistry.initCache(props);

        UCUMService.validate("[ft_i]");
        UCUMService.canonicalize("[ft_i]");
        UCUMService.canonicalize("S");
    }
}