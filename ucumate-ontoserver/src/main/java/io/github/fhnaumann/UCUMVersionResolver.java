package io.github.fhnaumann;

import au.csiro.ontoserver.VersionResolver;
import io.github.fhnaumann.model.UcumVersion;

/**
 * @author Felix Naumann
 */
public class UCUMVersionResolver implements VersionResolver {
    @Override
    public String resolveLatestVersionForCodeSystem(String codeSystemUri, String codeSystemVersion, boolean safeOnly) {
        if(!UCUMOntoOperationPlugin.UCUM_SYSTEM.equals(codeSystemUri)) {
            return null;
        }
        return UcumVersion.V2_2.getVersion();
    }
}
