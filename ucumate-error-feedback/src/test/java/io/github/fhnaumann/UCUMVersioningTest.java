package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UcumVersion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class UCUMVersioningTest {

    @Test
    public void test_when_v2_2_and_NTU_invalid_unit_then_message_does_suggest_NTU_unit() {
        UCUMService service = new UCUMService(UcumVersion.V2_2);
        assertThat(service.validate("[NTU]a"))
                .isExactlyInstanceOf(ValidatorService.Failure.class)
                .extracting(ValidatorService.Failure.class::cast)
                .extracting(ValidatorService.Failure::errorMessages)
                .satisfies(messages -> {
                    assertThat(messages).hasSize(1);
                    assertThat(messages.getFirst()).contains("'[NTU]a' is not a known unit but did you mean (or any of) '[NTU]' instead?");
                });
    }

    @Test
    public void test_when_v2_1_and_NTU_unit_then_message_does_not_suggest_NTU_unit() {
        UCUMService service = new UCUMService(UcumVersion.V2_1);
        assertThat(service.validate("[NTU]"))
                .isExactlyInstanceOf(ValidatorService.Failure.class)
                .extracting(ValidatorService.Failure.class::cast)
                .extracting(ValidatorService.Failure::errorMessages)
                .satisfies(messages -> {
                    assertThat(messages).hasSize(1);
                    assertThat(messages.getFirst()).doesNotContain("'[NTU]' is not a known unit but did you mean (or any of) '[NTU]' instead?");
                    assertThat(messages.getFirst()).contains("'[NTU]' could not be parsed to a stigmatized unit.");
                });
    }
}
