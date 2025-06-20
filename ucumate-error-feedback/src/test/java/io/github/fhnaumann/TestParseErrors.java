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
    private static final String UNIT_CORRECTION = "unit_correction";
    private static final String WRONG_MUL = "wrong_multiplication";
    private static final String WRONG_DIV = "wrong_division";
    private static final String BIN_TERM_MISSING_LHS = "binary_term_missing_lhs";
    private static final String BIN_TERM_MISSING_RHS = "binary_term_missing_rhs";
    private static final String MISSING_LEFT_PAREN = "binary_term_missing_left_paren";
    private static final String MISSING_RIGHT_PAREN = "binary_term_missing_right_paren";
    private static final String MISSING_L_SQB = "missing_left_square_bracket";
    private static final String MISSING_R_SQB = "missing_right_square_bracket";
    private static final String MISSING_L_CB = "missing_left_curly_bracket";
    private static final String MISSING_R_CB = "missing_right_curly_bracket";
    private static final String NEG_NUM = "negative_number";

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
                Arguments.of("mdsvb ", List.of(msg(SPACES), msg(UNIT_CORRECTION, "mdsvb ", "m"))),
                Arguments.of("ft_i", List.of(msg(UNIT_CORRECTION, "ft_i", "[ft_i]"))),
                Arguments.of("ft_us", List.of(msg(UNIT_CORRECTION, "ft_us", "[ft_us]"))),
                Arguments.of("m*s", List.of(msg(WRONG_MUL, "*"))),
                Arguments.of("m➗s", List.of(msg(WRONG_DIV, "➗"))),
                Arguments.of("m.s.", List.of(msg(BIN_TERM_MISSING_RHS, "m.s"))),
                Arguments.of("(.s)", List.of(msg(BIN_TERM_MISSING_LHS, "s"))),
                Arguments.of(".m.s", List.of(msg(BIN_TERM_MISSING_LHS, "m.s"))),
                Arguments.of(".m/s", List.of(msg(BIN_TERM_MISSING_LHS, "m/s"))),
                Arguments.of("m*s.", List.of(msg(BIN_TERM_MISSING_RHS, "m*s"), msg(WRONG_MUL, "*"))),
                Arguments.of("m.s*", List.of(msg(BIN_TERM_MISSING_RHS, "m.s"), msg(WRONG_MUL, "*"))),
                Arguments.of("(m", List.of(msg(MISSING_RIGHT_PAREN, "(m"))),
                Arguments.of("(m.s.g", List.of(msg(MISSING_RIGHT_PAREN, "(m.s.g"))),
                Arguments.of("((m.s).g", List.of(msg(MISSING_RIGHT_PAREN, "((m.s).g"))),
                Arguments.of("((((m.s).g", List.of(msg(MISSING_RIGHT_PAREN, "((((m.s).g"))),
                Arguments.of("m)", List.of(msg(MISSING_LEFT_PAREN, "m)"))),
                Arguments.of("m.s.g)", List.of(msg(MISSING_LEFT_PAREN, "m.s.g)"))),
                Arguments.of("m.s).g", List.of(msg(MISSING_LEFT_PAREN, "m.s)"))),
                Arguments.of("m2.(m.s).g).cd", List.of(msg(MISSING_LEFT_PAREN, "m2.(m.s).g)"))),
                Arguments.of("-5", List.of(msg(NEG_NUM, "-5"))),
                Arguments.of("-5.2.-3.-2", List.of(msg(NEG_NUM, "-5"), msg(NEG_NUM, "-3"), msg(NEG_NUM, "-2"))),
                Arguments.of("[ft_i", List.of(msg(MISSING_R_SQB, "[ft_i"))),
                Arguments.of("ft_i]", List.of(msg(MISSING_L_SQB, "ft_i]"))),
                Arguments.of("c[ft_i", List.of(msg(MISSING_R_SQB, "[ft_i"))),
                Arguments.of("cft_i]", List.of(msg(MISSING_L_SQB, "cft_i]"))),
                Arguments.of("m{abc", List.of(msg(MISSING_R_CB, "{abc"))),
                Arguments.of("mabc}", List.of(msg(MISSING_L_CB, "mabc}"), msg(UNIT_CORRECTION, "mabc}", "m"))),
                Arguments.of("{abc", List.of(msg(MISSING_R_CB, "{abc"))),
                Arguments.of("abc}", List.of(msg(MISSING_L_CB, "abc}"), msg(UNIT_CORRECTION, "abc}", "a")))
        );
    }

    private static String msg(String key, Object... placeholders) {
        return ErrorMessages.get(key, placeholders);
    }
}
