package me.fhnau.org;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestCaseLoader {

    private static final String DEFAULT_TEST_CASE_PATH = "ucum-tests.json";
    private static final String GRAHAM_FUNCTIONAL_TESTS_PATH = "UcumFunctionalTests.xml";

    public static TestSuite load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(TestCaseLoader.class.getClassLoader().getResourceAsStream(DEFAULT_TEST_CASE_PATH), TestSuite.class);
    }

    public static InputStream loadGrahamFunctionalTests() {
        return TestCaseLoader.class.getClassLoader().getResourceAsStream(GRAHAM_FUNCTIONAL_TESTS_PATH);
    }
}
