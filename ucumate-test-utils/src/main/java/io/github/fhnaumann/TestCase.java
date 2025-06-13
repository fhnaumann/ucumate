package io.github.fhnaumann;

import java.lang.module.Configuration;

public sealed interface TestCase {

    String id();

    public record ValidateTestCase(String id, String inputExpression, boolean valid, String reason) implements TestCase {
        @Override public String toString() {
            return "%s: %s -> %s".formatted(id, inputExpression, valid ? "valid" : "invalid");
        }
    }

    public record CommensurableTestCase(String id, String expr1, String expr2, boolean commensurable, String reason) implements TestCase {
        @Override public String toString() {
            return id + ": " + expr1 + " and " + expr2 + " are " + (!commensurable ? "not " : "") + "commensurable";
        }
    }

    public record ConvertTestCase(String id, String conversionFactor, String from, String to, String resultingConversionFactor, String substanceMolarMassCoeff, boolean valid, String reason) implements TestCase {

        private ConvertTestCase(String id, String conversionFactor, String from, String to, String resultingConversionFactor, boolean valid, String reason) {
            this(id, conversionFactor, from, to, resultingConversionFactor, null, valid, reason);
        }

        @Override public String toString() {
            if(substanceMolarMassCoeff != null) {
                return id + ": " + conversionFactor + ": " + from + " -> " + resultingConversionFactor + ": " + to + " with " + substanceMolarMassCoeff  +  (!reason.isEmpty() ? " Reason: " + reason : "");
            }
            return id + ": " + conversionFactor + ": " + from + " -> " + resultingConversionFactor + ": " + to + (!reason.isEmpty() ? " Reason: " + reason : "");
        }
    }

    public record CanonicalizeTestCase(String id, String inputExpression, String factor, String canonicalForm) implements TestCase {}
}
