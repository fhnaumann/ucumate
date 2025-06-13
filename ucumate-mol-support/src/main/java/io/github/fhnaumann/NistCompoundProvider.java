package io.github.fhnaumann;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.github.fhnaumann.compounds.CompoundProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Uses the nist compounds data.
 * Available <a href="https://github.com/IvanChernyshov/NistChemData/blob/main/data/nist_compounds.csv">here</a>.
 *
 * @author Felix Naumann
 */
public class NistCompoundProvider implements CompoundProvider {

    private static final Logger log = LoggerFactory.getLogger(NistCompoundProvider.class);
    private final Collection<MoleCompoundData> nistCompounds;
    private final Map<String, String> byName;
    private final Map<String, String> bySynonym;
    private final Map<String, String> byFormular;
    private final Map<String, String> byCasRn;
    private final Map<String, String> byInchiKey;

    public NistCompoundProvider() {
        this(createDefaultProvider());
    }

    public NistCompoundProvider(InputStream nistCompounds) {
        this.nistCompounds = loadNistCompounds(nistCompounds);
        this.byName = asMapWithKey(MoleCompoundData::name, this.nistCompounds);
        this.bySynonym = asMapWithKey(MoleCompoundData::synonyms, this.nistCompounds);
        this.byFormular = asMapWithKey(MoleCompoundData::formula, this.nistCompounds);
        this.byCasRn = asMapWithKey(MoleCompoundData::casRn, this.nistCompounds);
        this.byInchiKey = asMapWithKey(MoleCompoundData::inchiKey, this.nistCompounds);
    }

    private static InputStream createDefaultProvider() {
        InputStream in = NistCompoundProvider.class.getResourceAsStream("/nist_compounds.csv");
        if(in == null) {
            throw new RuntimeException("Failed to load default compounds!");
        }
        return in;
    }

    private static Map<String, String> asMapWithKey(Function<MoleCompoundData, String> keyGetter, Collection<MoleCompoundData> allCompounds) {
        return allCompounds.stream()
                //.peek(moleCompoundData -> System.out.println(moleCompoundData.molWeight()))
                .filter(moleCompoundData -> keyGetter.apply(moleCompoundData) != null)
                .filter(moleCompoundData -> moleCompoundData.molWeight() != null) // for some reason some rows don't have a mole weight, just ignore them.
                .collect(Collectors.toMap(
                        keyGetter,
                        MoleCompoundData::molWeight,
                        (s, s2) -> s
                ));
    }

    private static Collection<MoleCompoundData> loadNistCompounds(InputStream nistCompounds) {
        CsvMapper mapper = new CsvMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withNullValue("");
        try {
            return mapper.readerFor(MoleCompoundData.class)
                    .with(schema)
                    .readValues(nistCompounds)
                    .readAll()
                    .stream()
                    .map(MoleCompoundData.class::cast)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String findByName(String name) {
        log.warn("The name is not unique, returning first match.");
        return byName.get(name);
    }

    @Override
    public String findBySynonym(String synonym) {
        log.warn("The synonym is not unique, returning first match.");
        return bySynonym.get(synonym);
    }

    @Override
    public String findByFormular(String formular) {
        log.warn("The formular is not unique, returning first match.");
        return byFormular.get(formular);
    }

    @Override
    public String findByCasRn(String casRn) {
        return byCasRn.get(casRn);
    }

    @Override
    public String findByInchiKey(String inchiKey) {
        log.warn("The inchiKey is not unique, returning first match.");
        return byInchiKey.get(inchiKey);
    }

    @Override
    public String findByMatch(String value) {
        // try and match for unique key first
        String casRnMatch = findByCasRn(value);
        if(casRnMatch != null) {
            return casRnMatch;
        }
        // may return a wrong result due to multiple values for the given value
        String nameMatch = findByName(value);
        if(nameMatch != null) {
            return nameMatch;
        }
        String synonymMatch = findBySynonym(value);
        if(synonymMatch != null) {
            return synonymMatch;
        }
        String formularMatch = findByFormular(value);
        if(formularMatch != null) {
            return formularMatch;
        }
        String inchiKeyMatch = findByInchiKey(value);
        if(inchiKeyMatch != null) {
            return inchiKeyMatch;
        }
        return null;
    }
}
