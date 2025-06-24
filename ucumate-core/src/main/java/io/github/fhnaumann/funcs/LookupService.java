package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;

import java.util.*;

/**
 * @author Felix Naumann
 */
public interface LookupService {

    /**
     * Lookup a UCUM unit with other properties besides the code.
     * Here are the supported ways to look up a unit:
     * <br>
     * 1) Direct code match. It's the same as {@link ValidatorService#validate(String)} just the
     * returned object is a {@link io.github.fhnaumann.model.UCUMDefinition.UCUMUnit} instead of a
     * {@link io.github.fhnaumann.model.UCUMExpression.Term}.
     * <br>
     * 2) Case-insensitive code match. Using case-insensitive codes can lead to ambiguity, therefore multiple
     * matched units may be returned.
     * <br>
     * 3) Print symbol match. Unicode strings and numeric (and named) HTML entities are supported. Note that HTML tags
     * such as {@code <sub></sub>} are not supported. I.e. {@code °C}, {@code &#176;C}, and {@code &deg;C} all match
     * the Celsius unit. Contrary, {@code a<sub>t</sub>} and {@code aₜ} don't match the tropical year ("a_t") unit.
     * <br>
     * 4) Name match. Same rules from the print symbol matching apply here.
     * <br>
     * 5)Code without square brackets match. Matches units that have square brackets ({@code [} and {@code ]} in their
     * code. I.e. {@code ft_i} matches {@code [ft_i]}.
     * <br>
     * 6) Reapplies 2-5) but tests for contains match (no exact match) only.
     * <br>
     * 7) Dimension match. Matches the base unit with the provided dimension.
     * <br>
     * 8) Value match. Matches the unit that uses the provided UCUM expression in its unit definition if and only if
     * the factor in the definition is 1.
     * <br>
     * 9) Property match. Matches the provided property either with a direct match or contains match. This will return many
     * units.
     * <br>
     * @param input Any string input.
     * @param allowedMatchTypes Match types allowed to be returned.
     * @param comparator Used to sort the results.
     * @return A {@link LookupResult} containing information about the lookup.
     */
    public LookupResult lookup(String input, Collection<MatchType> allowedMatchTypes, Comparator<MatchType> comparator);

    public default LookupResult lookup(String input, Collection<MatchType> allowedMatchTypes) {
        return lookup(input, allowedMatchTypes, Comparator.comparingInt(MatchType::score));
    }

    public default LookupResult lookup(String input, Comparator<MatchType> comparator) {
        return lookup(input, Arrays.asList(BuiltInMatchType.values()), comparator);
    }

    public default LookupResult lookup(String input) {
        return lookup(input, Arrays.asList(BuiltInMatchType.values()));
    }

    public interface MatchType {
        int score();
        String name();
    }

    public enum BuiltInMatchType implements MatchType {
        CODE(0),
        CODE_CI(100),
        PRINT_SYMBOL_DIRECT(200),
        NAME_DIRECT(300),
        CODE_NO_SB_DIRECT(400),
        PRINT_SYMBOL_CONTAINS(500),
        NAME_CONTAINS(600),
        BASE_DIM(700),
        VALUE(800),
        PROPERTY_DIRECT(900),
        PROPERTY_CONTAINS(1000);

        private final int score;

        BuiltInMatchType(int score) {
            this.score = score;
        }

        @Override
        public int score() {
            return score;
        }
    }

    /**
     * Contains information about the lookup that was performed.
     */
    public sealed interface LookupResult {}

    /**
     * Represents a successful lookup indicating that something was matched.
     */
    public sealed interface Success extends LookupResult {}

    /**
     * A direct match with the code was found. This means the input string unambiguously represents exactly UCUM unit.
     * @param unit The matched UCUM unit.
     */
    public record DirectMatch(UCUMDefinition.UCUMUnit unit) implements Success {}

    /**
     * A direct match could not be made but some other match was found. The list may only have one element
     * in some situations. This is still different from a direct match because something other than the code was used to
     * match it.
     * @param units The matched UCUM unit(s).
     */
    public record MultipleMatches(List<UCUMDefinition.UCUMUnit> units) implements Success {}

    /**
     * No match could be made.
     */
    public record NoMatch() implements LookupResult {}
}
