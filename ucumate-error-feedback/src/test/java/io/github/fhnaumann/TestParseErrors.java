package io.github.fhnaumann;

import io.github.fhnaumann.model.UCUMExpression;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class TestParseErrors {

    private static final String SPACES = "spaces";
    private static final String PREFIX_MATCH_FAIL = "prefix_match_failure";
    private static final String UNIT_MATCH_FAIL = "unit_match_failure";

    @ParameterizedTest
    @MethodSource("provideInvalidInputs")
    public void test(String input, List<String> errorMessages) {
        Main.Result result = Main.visit(input);
        assertThat(result)
                .isInstanceOf(Main.Failure.class)
                .extracting(Main.Failure.class::cast)
                .extracting(Main.Failure::errorMessages)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .containsExactlyInAnyOrderElementsOf(errorMessages);

    }


    private static Stream<Arguments> provideInvalidInputs() {
        return Stream.of(
            Arguments.of("  ", List.of(msg(SPACES))),
            Arguments.of("m ", List.of(msg(SPACES))),
            Arguments.of("mdsvb ", List.of(msg(SPACES), msg(PREFIX_MATCH_FAIL, "mdsv"), msg(UNIT_MATCH_FAIL, "mdsvb")))
        );
    }

    private static String msg(String key, Object... placeholders) {
        return ErrorMessages.get(key, placeholders);
    }
}
