package io.github.fhnaumann;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.fhnaumann.compounds.CompoundProvider;
import io.github.fhnaumann.compounds.CompoundProviderRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
    @Disabled
    public void testByName(MoleCompoundData data) {
        assertThat(data).isNotNull();
        //assertThat(provider.findByName(data.name())).isEqualTo(data.name() != null ? data.molWeight() : null);
        //assertThat(provider.findBySynonym(data.synonyms())).isEqualTo(data.synonyms() != null ? data.molWeight() : null);
        //assertThat(provider.findByFormular(data.formula())).isEqualTo(data.formula() != null ? data.molWeight() : null);
        assertThat(provider.findByCasRn(data.casRn())).isEqualTo(data.casRn() != null ? data.molWeight() : null);
        // assertThat(provider.findByInchiKey(data.inchiKey())).isEqualTo(data.inchiKey() != null ? data.molWeight() : null);
        //assertThat(provider.findByMatch(data.name())).isEqualTo(data.name() != null ? data.molWeight() : null);
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
