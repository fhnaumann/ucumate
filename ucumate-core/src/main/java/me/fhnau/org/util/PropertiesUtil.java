package me.fhnau.org.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Felix Naumann
 */
public class PropertiesUtil {

    public static List<String> readCodeFile(Path path) throws IOException {
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".txt")) {
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        }

        if (fileName.endsWith(".json")) {
            try (InputStream is = Files.newInputStream(path)) {
                return new ObjectMapper().readValue(is, new TypeReference<>() {});
            }
        }
        throw new IOException("Unsupported file type: " + path);
    }

}
