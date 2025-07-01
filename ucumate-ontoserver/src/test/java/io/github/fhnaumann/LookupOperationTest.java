package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.operations.LookupOperation;
import io.github.fhnaumann.operations.ucum.UCUMLookupOperation;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class LookupOperationTest {


    private LookupOperation plugin;

    @BeforeEach
    public void setup() {
        plugin = new UCUMLookupOperation(new UCUMService());
    }

    @ParameterizedTest
    @MethodSource("provide_lookup_correct_inputs")
    public void test_lookup_returns_correct_result_on_simple_call(String inputCode, String expectedDisplay, Map<String, String> props) {
        LookupOperation.Success success = perform_lookup(inputCode, new ArrayList<>(props.keySet()));
        assert_success_matches(success,
                inputCode,
                expectedDisplay,
                props);
    }

    private static Stream<Arguments> provide_lookup_correct_inputs() {
        return Stream.of(
                Arguments.of("m", "m", Map.of("unitName", "meter")),
                Arguments.of("mm", "mm", Map.of("unitName", "millimeter")),
                Arguments.of("m.s", "m.s", Map.of()),
                Arguments.of("g{abc}", "g{abc}", Map.of())
        );
    }

    @Test
    public void test_lookup_returns_operation_outcome_on_invalid_code() {
        LookupOperation.Failure failure = perform_invalid_lookup("not_a_code", UcumVersion.V2_2.getVersion());
        assert_operation_outcome(failure.operationOutcome(), OperationOutcome.IssueType.CODEINVALID);
    }

    @Test
    public void test_lookup_returns_operation_outcome_on_invalid_version() {
        LookupOperation.Failure failure = perform_invalid_lookup("m", "1.9");
        assert_operation_outcome(failure.operationOutcome(), OperationOutcome.IssueType.NOTSUPPORTED);
    }

    @Test
    public void test_lookup_silently_ignores_unknown_properties() {
        LookupOperation.Success success = perform_lookup("m", List.of("not_a_real_prop", "unitName"));
        assertThat(success.returnedProperties()).doesNotContainKey("not_a_real_prop");
    }

    private static void assert_operation_outcome(OperationOutcome actual, OperationOutcome.IssueType expectedIssueType) {
        assertThat(actual.hasIssue()).isTrue();
        assertThat(actual.getIssue().size()).isEqualTo(1);
        assertThat(actual.getIssueFirstRep().getCode()).isEqualTo(expectedIssueType);
    }

    private static void assert_success_matches(LookupOperation.Success actual, String code, String display, Map<String, String> props) {
        assertThat(actual.code()).isEqualTo(code);
        assertThat(actual.display()).isEqualTo(display);
        assertThat(actual.returnedProperties()).isEqualTo(props);
    }

    private LookupOperation.Failure perform_invalid_lookup(String code, String version) {
        return (LookupOperation.Failure) plugin.lookup(new Coding().setCode(code).setVersion(version), List.of());
    }

    private LookupOperation.Success perform_lookup(String code, List<String> props) {
        return perform_lookup(code, UcumVersion.V2_2.getVersion(), props);
    }

    private LookupOperation.Success perform_lookup(String code, String version, List<String> props) {
        return ((LookupOperation.Success) plugin.lookup(new Coding().setCode(code).setVersion(version), props));
    }
}
