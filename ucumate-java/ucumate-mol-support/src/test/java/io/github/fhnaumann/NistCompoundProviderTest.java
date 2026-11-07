package io.github.fhnaumann;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.fhnaumann.compounds.CompoundProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class NistCompoundProviderTest {

    private static CompoundProvider provider;

    @BeforeAll
    public static void setUp() {
        provider = new NistCompoundProvider();
    }

    @ParameterizedTest
    @MethodSource("nistRows")
    @Disabled("Too many tests for CI.")
    public void testByName(MoleCompoundData data) {
        assertThat(data).isNotNull();
        assertThat(provider.findByCasRn(data.casRn())).isEqualTo(data.casRn() != null ? data.molWeight() : null);
    }

    static Stream<MoleCompoundData> nistRows() throws IOException {
        InputStream in = NistCompoundProviderTest.class.getResourceAsStream("/nist_compounds.csv");
        CsvMapper mapper = new CsvMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withNullValue("");

        List<MoleCompoundData> compounds = mapper.readerFor(MoleCompoundData.class)
                .with(schema)
                .readValues(in)
                .readAll()
                .stream()
                .map(MoleCompoundData.class::cast)
                .toList();
        return compounds.stream();
    }


}
