package io.github.fhnaumann.model;

import io.github.fhnaumann.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Felix Naumann
 */
public enum UcumVersion {
    V2_2("2.2"),
    V2_1("2.1");

    private static final Logger log = LoggerFactory.getLogger(UcumVersion.class);
    final String version;

    UcumVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public static UcumVersion fromVersionString(String version) {
        return switch (version) {
            case "2.2" -> V2_2;
            case "2.1"-> V2_1;
            default -> LogUtil.logAndThrow(log, "Unknown UCUM version {}.", version);
        };
    }
}
