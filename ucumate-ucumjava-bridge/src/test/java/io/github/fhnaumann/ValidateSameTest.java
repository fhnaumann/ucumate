package io.github.fhnaumann;

import io.github.fhnaumann.funcs.Validator;
import org.fhir.ucum.utils.XmlUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * @author Felix Naumann
 */
public class ValidateSameTest extends UcumateToUcumJavaTestBase {

    /*
    These are "wrong" and therefore ucumate has (partially) different results. See the online documentation of ucumate
    to see the individual reasoning for each test case that is listed here.
     */
    private static final List<String> TESTS_TO_SKIP = List.of("1-108", "3-115", "3-121", "3-122", "3-123", "3-124", "3-128");


    @DisplayName("UCUM Functional Tests loaded from UcumFunctionalTests.xml")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("getTestXMLElements")
    public void testXmlElement(String testName, String testType, Element testCase) throws IOException {
        if (testType.equals("validation")) {
            runValidationCase(testCase);
        }

    }


    private void runValidationCase(Element x) {
        String id = x.getAttribute("id");
        String unit = x.getAttribute("unit");
        boolean valid = "true".equals(x.getAttribute("valid"));
        String reason = x.getAttribute("reason");

        assumeFalse(TESTS_TO_SKIP.contains(id), "Skipping test %s because it was marked so. See the ucumate online documentation to find out why.".formatted(id));

        boolean expectedValid = oldService.validate(unit) == null;
        Validator.ValidationResult result = Validator.validate(unit);
        if(expectedValid) {
            assertThat(result)
                    .withFailMessage("Unit %s was expected to be valid, but was invalid (%s)".formatted(unit, id))
                    .isInstanceOf(Validator.Success.class);
        }
        else {
            assertThat(result)
                    .withFailMessage("Unit %s was expected to be invalid, but was valid (%s)".formatted(unit, id))
                    .isInstanceOf(Validator.Failure.class);
        }
    }


    public static Stream<Arguments> getTestXMLElements() throws IOException, ParserConfigurationException, SAXException {
        List<Arguments> elements = new ArrayList<>();
        Document doc = XmlUtils.parseDOM(TestCaseLoader.loadGrahamFunctionalTests());
        Element element = doc.getDocumentElement();

        if (!element.getNodeName().equals("ucumTests"))
            throw new IllegalStateException("Unable to process XML document: expected 'ucumTests' but found '"+element.getNodeName()+"'");

        Element focus = XmlUtils.getFirstChild(element);
        while (focus != null) {

            String testTypeString = focus.getNodeName();
            for (Element testCase : XmlUtils.getNamedChildren(focus, "case"))
            {
                String testId = testCase.getAttribute("id");
                elements.add(Arguments.of(testTypeString + ": " + testId, testTypeString, testCase));
            }
            focus = XmlUtils.getNextSibling(focus);

        }
        return elements.stream();
    }
}
