package io.github.fhnaumann.compounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * @author Felix Naumann
 */
public class CompoundUtil {

    private static final Logger log = LoggerFactory.getLogger(CompoundUtil.class);

    public static String resolveMolarMass(String input) {
        if(input == null) {
            // if no coefficient provided, don't do anything
            return null;
        }
        if (isNumeric(input)) {
            return input;
        }
        // Try optional CompoundProvider
        CompoundProvider provider = CompoundProviderRegistry.get();
        String resolved = provider.findByMatch(input);
        if (resolved != null && isNumeric(resolved)) {
            return resolved;
        }
        throw new IllegalArgumentException("Invalid input. If the provided molar mass is not a number, then an appropriate CompoundProvider implementation has to be used. Did you forget to add the ucumate-mol-support dependency?");
    }

    private static boolean isNumeric(String s) {
        try {
            new BigDecimal(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
