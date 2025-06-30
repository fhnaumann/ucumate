package io.github.fhnaumann;

import java.io.InputStream;

/**
 * @author Felix Naumann
 */
public class ResourceLoader {

    public static InputStream load(String filename) {
        return ResourceLoader.class.getClassLoader().getResourceAsStream(filename);
    }
}
