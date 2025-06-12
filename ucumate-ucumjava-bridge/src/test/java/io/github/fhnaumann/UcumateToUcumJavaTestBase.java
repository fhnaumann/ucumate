package io.github.fhnaumann;

import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author Felix Naumann
 */
public class UcumateToUcumJavaTestBase {

    protected static UcumEssenceService oldService;
    protected static UcumateToUcumJavaService newService;

    @BeforeAll
    public static void init() throws UcumException {
        oldService = new UcumEssenceService(UcumateToUcumJavaTestBase.class.getResourceAsStream("/ucum-essence.xml"));
        newService = new UcumateToUcumJavaService(UcumateToUcumJavaTestBase.class.getResourceAsStream("/ucum-essence.xml"));
    }
}
