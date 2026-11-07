package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class FeedbackValidatorSPITest {

    @Test
    public void test_spi_provider_is_auto_discovered_when_accessing_UCUMService() {
        UCUMService service = new UCUMService();
        assertThat(service.getValidatorService())
                .withFailMessage("Failed to auto discover FeedbackValidator from SPI META-INF services.")
                .isExactlyInstanceOf(FeedbackValidator.class);
    }
}
