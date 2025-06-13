package io.github.fhnaumann;

import org.fhir.ucum.UcumEssenceService;
import org.fhir.ucum.UcumException;

/**
 * @author Felix Naumann
 */
public class Main {
    public static void main(String[] args) throws UcumException {
        System.out.println("Hello, World!");

        UcumEssenceService legacyService = new UcumEssenceService(Main.class.getResourceAsStream("/ucum-essence.xml"));
        System.out.println(legacyService.analyse("Hz/g.N"));
        System.out.println(legacyService.validateCanonicalUnits("[ft_i]/a", "m+1.s-1"));
        System.out.println(legacyService.getCanonicalUnits("[ft_i]/a"));
        System.out.println(legacyService.getDefinedForms("1/s"));
    }
}