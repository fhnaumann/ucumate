package me.fhnau.org;

import me.fhnau.org.funcs.UCUMService;
import me.fhnau.org.persistence.InMemoryCanonicalizePersistenceProvider;
import me.fhnau.org.persistence.PersistenceRegistry;

public class Main {

    public static void main(String[] args) {
        //PersistenceRegistry.register("cache-canon", new InMemoryCanonicalizePersistenceProvider());
        UCUMService.canonicalize("S");
        System.out.println(UCUMService.canonicalize("s+2"));
    }
}
