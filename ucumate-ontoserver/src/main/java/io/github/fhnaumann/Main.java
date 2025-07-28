package io.github.fhnaumann;

/**
 * @author Felix Naumann
 */
public class Main {
    public static void main(String[] args) {
        /*
        Required operations:
        CodeSystem-lookup
        CodeSystem-validate-code
        CodeSystem-subsumes
        ValueSet-expand
        ValueSet-validate-code
        (ConceptMap-translate)


        Interesting classes:

        IValidationSupport -> lookup, expand
        ITermReadSvc -> expand, findCode, findCodesBelow, findCodesAbove, subsumes
        IFhirResourceDaoCodeSystem -> lookupCode, subsumes, validateCode
        RemoteTerminologyServiceValidationSupport
        InMemoryTerminologyServerValidationSupport
        ValueSetExpansionFilterContext -> useful for some ValueSet operation parameters?
        CommonCodeSystemsTerminologyService#lookupUcumCode -> Current UCUM support impl in HAPI FHIR
         */
    }
}