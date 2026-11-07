package io.github.fhnaumann;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class MiscSameTest extends UcumateToUcumJavaTestBase {

    @Test
    public void test_get_properties_returns_same() {
        Set<String> expectedProperties = oldService.getProperties();
        Set<String> actualProperties = newService.getProperties();
        assertThat(actualProperties).containsExactlyInAnyOrderElementsOf(expectedProperties);
    }
}
