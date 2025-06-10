package io.github.fhnaumann.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Felix Naumann
 */
public class PropertiesUtil {

    public static List<String> readCodeFile(InputStream inputStream) throws IOException {
        return new ObjectMapper().readValue(inputStream, new TypeReference<>() {});
    }

    public static List<String> readCodeFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new FileNotFoundException("File not found: " + filePath);

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".txt")) {
            return Files.readAllLines(file.toPath()).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        }

        if (fileName.endsWith(".json")) {
            try (InputStream is = new FileInputStream(file)) {
                return new ObjectMapper().readValue(is, new TypeReference<>() {});
            }
        }

        throw new IOException("Unsupported file type: " + fileName);
    }

}
