package io.github.fhnaumann;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

/**
 * @author Felix Naumann
 */
public class PluginUtil {


    public static CodeableConcept coding2CodeableConcept(Coding coding) {
        return new CodeableConcept(coding);
    }
}
