package me.fhnau.org;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TestCaseLoader {

    private static final String DEFAULT_TEST_CASE_PATH = "ucum-tests.json";

    public static TestSuite load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(TestCaseLoader.class.getClassLoader().getResourceAsStream(DEFAULT_TEST_CASE_PATH), TestSuite.class);
    }
}
