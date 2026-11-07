package io.github.fhnaumann.compounds;

/**
 * @author Felix Naumann
 */
public interface CompoundProvider {

    /**
     * Find the mol weight by a given compound name.
     * @param name The compound name.
     * @return The mol mass for the matched compound or null.
     */
    String findByName(String name);

    /**
     * Find the mol weight by a given compound synonym name.
     * @param synonym The compound synonym name.
     * @return The mol mass for the matched compound or null.
     */
    String findBySynonym(String synonym);

    /**
     * Find the mol weight by a given formular.
     * @param formular The compound formular.
     * @return The mol mass for the matched compound or null.
     */
    String findByFormular(String formular);

    /**
     * Find the mol weight by a given compound casRn.
     * @param casRn The compound casRn.
     * @return The mol mass for the matched compound or null.
     */
    String findByCasRn(String casRn);

    /**
     * Find the mol weight by a given inchi key.
     * @param inchiKey The inchi key.
     * @return The mol mass for the matched compount or null.
     */
    String findByInchiKey(String inchiKey);

    /**
     * Find the mol weight by a given string.
     * The string is matched against any available keys, these could be:
     * name, synonym, formular, casRn, inchiKey, or any other custom implementation
     * @param value The string used as a key.
     * @return The mol mass for the matched compound or null.
     */
    String findByMatch(String value);
}
