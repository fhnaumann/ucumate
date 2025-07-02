package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.operations.ValidateCodeOperation;
import io.github.fhnaumann.operations.ucum.UCUMExpandOperation;
import io.github.fhnaumann.operations.ucum.UCUMValidateCodeOperation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class ValidateCodeOperationTest {

    private ValidateCodeOperation plugin;

    @BeforeEach
    public void setup() {
        UCUMService service = new UCUMService();
        plugin = new UCUMValidateCodeOperation(service, new UCUMExpandOperation(service));
    }

    @Test
    public void test_code_is_valid() {
        Coding coding = coding("m");
        ValidateCodeOperation.Success success = perform_validate_code(coding);
        assertThat(success.result()).isTrue();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isTrue();
        assertThat(detail.message()).isNull();
        assertThat(detail.display()).isEqualTo("m");
    }

    @Test
    public void test_complex_code_is_valid() {
        Coding coding = coding("m.s");
        ValidateCodeOperation.Success success = perform_validate_code(coding);
        assertThat(success.result()).isTrue();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isTrue();
        assertThat(detail.message()).isNull();
        assertThat(detail.display()).isEqualTo("m.s");
    }

    @Test
    public void test_warning_message_for_annotations() {
        Coding coding = coding("m{a}.s");
        ValidateCodeOperation.Success success = perform_validate_code(coding);
        assertThat(success.result()).isTrue();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isTrue();
        assertThat(detail.message()).isEqualTo("m{a}.s: The usage of annotations in UCUM expressions is discouraged.");
        assertThat(detail.display()).isEqualTo("m{a}.s");
    }

    @Test
    public void test_error_message_for_invalid_code() {
        Coding coding = coding("not_a_code");
        ValidateCodeOperation.Success success = perform_validate_code(coding);
        assertThat(success.result()).isFalse();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isFalse();
        assertThat(detail.message()).isEqualTo("The code 'not_a_code' is invalid: 'not_a_code' did not match any known unit.");
        assertThat(detail.display()).isNull();
    }

    @Test
    public void test_error_message_for_invalid_display_with_valid_code() {
        Coding coding = coding("m.s");
        coding.setDisplay("not_a_valid_display");
        ValidateCodeOperation.Success success = perform_validate_code(coding);
        assertThat(success.result()).isFalse();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isFalse();
        assertThat(detail.message()).isEqualTo("The display 'not_a_valid_display' does not match the provided code 'm.s'. In UCUM code and display are the same.");
        assertThat(detail.display()).isNull();
    }

    @Test
    public void test_code_and_display_are_valid() {
        Coding coding = coding("m");
        coding.setDisplay("m");
        ValidateCodeOperation.Success success = perform_validate_code(coding);
        assertThat(success.result()).isTrue();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isTrue();
        assertThat(detail.message()).isNull();
        assertThat(detail.display()).isEqualTo("m");
    }

    @Test
    public void test_code_is_valid_when_in_provided_vs() {
        ValueSet vs = create_vs_with("m", "cm", "s", "kg", "g");
        Coding coding = coding("s");
        ValidateCodeOperation.Success success = perform_validate_code(vs, coding);
        assertThat(success.result()).isTrue();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isTrue();
        assertThat(detail.message()).isNull();
    }

    @Test
    public void test_code_is_invalid_when_syntax_valid_but_not_in_provided_vs() {
        ValueSet vs = create_vs_with("m", "cm", "s", "kg", "g");
        Coding coding = coding("[ft_i]");
        ValidateCodeOperation.Success success = perform_validate_code(vs, coding);
        assertThat(success.result()).isFalse();
        ValidateCodeOperation.Detail detail = success.details().get(coding);
        assertThat(detail.valid()).isFalse();
        assertThat(detail.message()).isEqualTo("The code '[ft_i]' is invalid: '[ft_i]' is valid itself but it is not in the expanded ValueSet.");
    }

    @Test
    public void test_error_message_if_at_least_one_code_is_invalid() {
        Coding valid1 = coding("m");
        Coding valid2 = coding("s");
        Coding invalid1 = coding("not_a_code");
        ValidateCodeOperation.Success success = perform_validate_code(valid1, valid2, invalid1);
        assertThat(success.result()).isFalse();
        ValidateCodeOperation.Detail invalidDetail = success.details().get(invalid1);
        assertThat(invalidDetail.valid()).isFalse();
        assertThat(invalidDetail.message()).isEqualTo("The code 'not_a_code' is invalid: 'not_a_code' did not match any known unit.");
    }

    @Test
    public void test_warning_message_if_at_least_one_code_has_annotation() {
        Coding valid1 = coding("m");
        Coding valid2 = coding("s");
        Coding warning1 = coding("g{annot}");
        ValidateCodeOperation.Success success = perform_validate_code(valid1, valid2, warning1);
        assertThat(success.result()).isTrue();
        ValidateCodeOperation.Detail warningDetail = success.details().get(warning1);
        assertThat(warningDetail.valid()).isTrue();
        assertThat(warningDetail.message()).isEqualTo("g{annot}: The usage of annotations in UCUM expressions is discouraged.");
    }

    @Test
    public void test_error_and_warning_are_both_present() {
        Coding valid1 = coding("m");
        Coding invalid1 = coding("not_a_code");
        Coding invalid2 = coding("s");
        invalid2.setDisplay("not_the_correct_display_for_s");
        Coding warning1 = coding("g{annot}");
        ValidateCodeOperation.Success success = perform_validate_code(valid1, invalid1, invalid2, warning1);
        assertThat(success.result()).isFalse();
        ValidateCodeOperation.Detail invalidDetail = success.details().get(invalid1);
        assertThat(invalidDetail.valid()).isFalse();
        assertThat(invalidDetail.message()).isEqualTo("The code 'not_a_code' is invalid: 'not_a_code' did not match any known unit.");
        ValidateCodeOperation.Detail invalidDetail2 = success.details().get(invalid2);
        assertThat(invalidDetail2.valid()).isFalse();
        assertThat(invalidDetail2.message()).isEqualTo("The display 'not_the_correct_display_for_s' does not match the provided code 's'. In UCUM code and display are the same.");
        ValidateCodeOperation.Detail warningDetail = success.details().get(warning1);
        assertThat(warningDetail.valid()).isTrue();
        assertThat(warningDetail.message()).isEqualTo("g{annot}: The usage of annotations in UCUM expressions is discouraged.");
    }

    private ValidateCodeOperation.Success perform_validate_code(Coding... codings) {
        CodeableConcept codeableConcept = new CodeableConcept();
        Arrays.stream(codings).forEach(codeableConcept::addCoding);
        return (ValidateCodeOperation.Success) plugin.validate(ucum_vs(), codeableConcept);
    }

    private ValidateCodeOperation.Success perform_validate_code(ValueSet vs, Coding coding) {
        return (ValidateCodeOperation.Success) plugin.validate(vs, coding);
    }

    private ValidateCodeOperation.Success perform_validate_code(Coding coding) {
        return (ValidateCodeOperation.Success) plugin.validate(ucum_vs(), coding);
    }

    private ValueSet create_vs_with(String... codes) {
        return create_vs_with(Arrays.stream(codes).map(s -> new Coding(UCUMOntoOperationPlugin.UCUM_SYSTEM, s, s)).toList());
    }

    private ValueSet create_vs_with(Coding... codings) {
        return create_vs_with(Arrays.stream(codings).toList());
    }

    private ValueSet create_vs_with(List<Coding> codings) {
        ValueSet vs = ucum_vs();
        List<ValueSet.ConceptReferenceComponent> concepts = codings.stream()
                .map(coding -> new ValueSet.ConceptReferenceComponent()
                        .setCode(coding.getCode())
                        .setDisplay(coding.getDisplay())
                )
                .toList();
        vs.getCompose().addInclude().setConcept(concepts);
        return vs;
    }

    private static Coding coding(String code) {
        return new Coding(UCUMOntoOperationPlugin.UCUM_SYSTEM, code, null);
    }

    private ValueSet ucum_vs() {
        ValueSet vs = new ValueSet();
        return vs;
    }
}
