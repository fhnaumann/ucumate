package io.github.fhnaumann;

import io.github.fhnaumann.funcs.Converter;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.PreciseDecimal;
import io.github.fhnaumann.util.UCUMRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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

@Tag("functional-tests")
public class UCUMFunctionalTests {

    private static UCUMRegistry registry;

    /*
    These are "wrong" and therefore ucumate has (partially) different results. See the online documentation of ucumate
    to see the individual reasoning for each test case that is listed here.
     */
    private static List<String> TESTS_TO_SKIP = List.of("1-108", "3-115", "3-121", "3-122", "3-123", "3-124", "3-128");

    @BeforeAll
    public static void init() {
        registry = UCUMRegistry.getInstance();
    }

    private enum FunctionalTestType {
        HISTORY,
        VALIDATION,
        DISPLAY_NAME_GENERATION,
        CONVERSION,
        DIVISION,
        MULTIPLICATION
    }

    @DisplayName("UCUM Functional Tests loaded from UcumFunctionalTests.xml")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("getTestXMLElements")
    public void testXmlElement(String testName, FunctionalTestType testType, Element testCase) throws IOException {
        if (testType.equals(FunctionalTestType.HISTORY))
            // Test history element. Not a test. Do nothing.
            ;
        else if (testType.equals(FunctionalTestType.VALIDATION))
            runValidationCase(testCase);
        else if (testType.equals(FunctionalTestType.CONVERSION))
            runConversionCase(testCase);
    }

    private void runDivisionCase(Element x) {

    }

    private void runMultiplicationCase(Element x) {
        String id = x.getAttribute("id");
        String v1 = x.getAttribute("v1");
        String u1 = x.getAttribute("u1");
        String v2 = x.getAttribute("v2");
        String u2 = x.getAttribute("u2");
        String vRes = x.getAttribute("vRes");
        String uRes = x.getAttribute("uRes");
        PreciseDecimal pd1 = new PreciseDecimal(v1, true);
        PreciseDecimal pd2 = new PreciseDecimal(v2, true);
        UCUMExpression.Term term1 = ((Validator.Success) Validator.validate(u1)).term();
        UCUMExpression.Term term2 = ((Validator.Success) Validator.validate(u2)).term();

    }

    private void runConversionCase(Element x) {
        String id = x.getAttribute("id");
        String value = x.getAttribute("value");
        String srcUnit = x.getAttribute("srcUnit");
        String dstUnit = x.getAttribute("dstUnit");
        String outcome = x.getAttribute("outcome");

        assumeFalse(TESTS_TO_SKIP.contains(id), "Skipping test %s because it was marked so. See the ucumate online documentation to find out why.".formatted(id));

        Converter.ConversionResult result = UCUMService.convert(new PreciseDecimal(value, false), srcUnit, dstUnit);
        //Converter.ConversionResult result = new Converter().convert(new Converter.Conversion(new PreciseDecimal(value, true), fromTerm), toTerm);
        assertThat(result)
                .withFailMessage("Test %s is not of instance success for conversion".formatted(id))
                .isInstanceOf(Converter.Success.class);
        PreciseDecimal convFactor = ((Converter.Success) result).conversionFactor();

        //TestUtil.skipIfRoundingProblem(outcome, convFactor);

        assertThat(convFactor.toString())
                .withFailMessage("Test %s: The value '%s' was expected the result was '%s'".formatted(id, outcome, convFactor))
                .startsWith(outcome);

    }

    private void runDisplayNameGenerationCase(Element x) {

    }

    private void runValidationCase(Element x) {
        String id = x.getAttribute("id");
        String unit = x.getAttribute("unit");
        boolean valid = "true".equals(x.getAttribute("valid"));
        String reason = x.getAttribute("reason");

        assumeFalse(TESTS_TO_SKIP.contains(id), "Skipping test %s because it was marked so. See the ucumate online documentation to find out why.".formatted(id));

        Validator.ValidationResult result = Validator.validate(unit);
        if(valid) {
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

    private static FunctionalTestType getFunctionalTestType(String typeString) {
        if (typeString.equals("history"))
            return FunctionalTestType.HISTORY;
        else if (typeString.equals("validation"))
            return FunctionalTestType.VALIDATION;
        else if (typeString.equals("displayNameGeneration"))
            return FunctionalTestType.DISPLAY_NAME_GENERATION;
        else if (typeString.equals("conversion"))
            return  FunctionalTestType.CONVERSION;
        else if (typeString.equals("multiplication"))
            return FunctionalTestType.MULTIPLICATION;
        else if (typeString.equals("division"))
            return FunctionalTestType.DIVISION;
        else
            throw new IllegalArgumentException("unknown element name "+ typeString);
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
            FunctionalTestType testType = getFunctionalTestType(testTypeString);
            for (Element testCase : XmlUtils.getNamedChildren(focus, "case"))
            {
                String testId = testCase.getAttribute("id");
                elements.add(Arguments.of(testTypeString + ": " + testId, testType, testCase));
            }
            focus = XmlUtils.getNextSibling(focus);

        }
        return elements.stream();
    }
}
