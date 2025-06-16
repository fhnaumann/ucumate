package io.github.fhnaumann;

import org.fhir.ucum.Concept;
import org.fhir.ucum.ConceptKind;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class SearchSameTest extends UcumateToUcumJavaTestBase {

    @ParameterizedTest
    @MethodSource("provide_searches")
    public void test_search_returns_same(ConceptKind kind, String text, boolean regex) {
        List<Concept> expectedConcepts = oldService.search(kind, text, regex);
        List<Concept> actualConcepts = newService.search(kind, text, regex);
        assertThat(actualConcepts.stream().map(Concept::toString).toList())
                .containsExactlyInAnyOrderElementsOf(expectedConcepts.stream().map(Concept::toString).toList());
    }

    private static Stream<Arguments> provide_searches() {
        return Stream.of(
                /*
                Prefixes
                 */
                Arguments.of(ConceptKind.PREFIX, "Ki", false),
                Arguments.of(ConceptKind.PREFIX, "Mi", false),
                Arguments.of(ConceptKind.PREFIX, "Gi", false),
                Arguments.of(ConceptKind.PREFIX, "Ti", false),
                Arguments.of(ConceptKind.PREFIX, "Y", false),
                Arguments.of(ConceptKind.PREFIX, "Z", false),
                Arguments.of(ConceptKind.PREFIX, "E", false),
                Arguments.of(ConceptKind.PREFIX, "P", false),
                Arguments.of(ConceptKind.PREFIX, "T", false),
                Arguments.of(ConceptKind.PREFIX, "G", false),
                Arguments.of(ConceptKind.PREFIX, "M", false),
                Arguments.of(ConceptKind.PREFIX, "k", false),
                Arguments.of(ConceptKind.PREFIX, "h", false),
                Arguments.of(ConceptKind.PREFIX, "da", false),
                Arguments.of(ConceptKind.PREFIX, "d", false),
                Arguments.of(ConceptKind.PREFIX, "c", false),
                Arguments.of(ConceptKind.PREFIX, "m", false),
                Arguments.of(ConceptKind.PREFIX, "u", false),
                Arguments.of(ConceptKind.PREFIX, "n", false),
                Arguments.of(ConceptKind.PREFIX, "p", false),
                Arguments.of(ConceptKind.PREFIX, "f", false),
                Arguments.of(ConceptKind.PREFIX, "a", false),
                Arguments.of(ConceptKind.PREFIX, "z", false),
                Arguments.of(ConceptKind.PREFIX, "y", false),
                Arguments.of(ConceptKind.PREFIX, "KIB", false),
                Arguments.of(ConceptKind.PREFIX, "MIB", false),
                Arguments.of(ConceptKind.PREFIX, "TIB", false),
                Arguments.of(ConceptKind.PREFIX, "YA", false),
                Arguments.of(ConceptKind.PREFIX, "ZA", false),
                Arguments.of(ConceptKind.PREFIX, "EX", false),
                Arguments.of(ConceptKind.PREFIX, "PT", false),
                Arguments.of(ConceptKind.PREFIX, "TR", false),
                /*
                Base Units
                 */
                Arguments.of(ConceptKind.BASEUNIT, "m", false),
                Arguments.of(ConceptKind.BASEUNIT, "M", false),
                Arguments.of(ConceptKind.BASEUNIT, "s", false),
                Arguments.of(ConceptKind.BASEUNIT, "S", false),
                Arguments.of(ConceptKind.BASEUNIT, "g", false),
                Arguments.of(ConceptKind.BASEUNIT, "G", false),
                Arguments.of(ConceptKind.BASEUNIT, "rad", false),
                Arguments.of(ConceptKind.BASEUNIT, "RAD", false),
                Arguments.of(ConceptKind.BASEUNIT, "K", false),
                Arguments.of(ConceptKind.BASEUNIT, "C", false),
                Arguments.of(ConceptKind.BASEUNIT, "cd", false),
                Arguments.of(ConceptKind.BASEUNIT, "CD", false),
                Arguments.of(ConceptKind.BASEUNIT, "m", true),
                /*
                Defined units
                 */
                Arguments.of(ConceptKind.UNIT, "[ft_i]", false),
                Arguments.of(ConceptKind.UNIT, "ngt", true),
                /*
                Misc (Combinations)
                 */
                Arguments.of(null, "f^", true),
                Arguments.of(null, "n$", true)

        );
    }
}
