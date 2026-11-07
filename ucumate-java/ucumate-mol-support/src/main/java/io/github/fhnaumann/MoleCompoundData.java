package io.github.fhnaumann;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Felix Naumann
 */
public record MoleCompoundData(
       @JsonProperty("ID") String id,
        @JsonProperty("name") String name,
        @JsonProperty("synonyms") String synonyms,
        @JsonProperty("formula") String formula,
        @JsonProperty("cas_rn") String casRn,
        @JsonProperty("inchi_key") String inchiKey,
        @JsonProperty("mol_weight") String molWeight
) {
}
