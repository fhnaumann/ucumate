package io.github.fhnaumann;

import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;
import org.junit.jupiter.api.BeforeAll;

import java.util.stream.Stream;

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

    protected static Stream<String> provide_units() {
        return Stream.of(
                "m",
                "cm",
                "m.s",
                "m/s",
                "[lton_av]",
                "cm2",
                "cm-2",
                "cm+0",
                "cm-0",
                "cm1",
                "[ft_i]",
                "2.[ft_i]/s",
                "(m.s).g",
                "m/s/g",
                "(m/s)/g",
                "m/(s/g)",
                "m/(s.g)",
                "m/s.g",
                "{}",
                "{abc}",
                "m{abc}",
                "cm{abc}",
                "(m{}.s{abc}).g{deb}",
                "(m2{}.s-3{abc}).g0{deb}",
                "/m",
                "/(m.s)",
                "mol",
                "1"
        );
    }
}
