package io.github.fhnaumann;

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

    public record ConvertTestCase(String id, String conversionFactor, String from, String to, String resultingConversionFactor, boolean valid, String reason) implements TestCase {
        @Override public String toString() {
            return id + ": " + conversionFactor + ": " + from + " -> " + resultingConversionFactor + ": " + to + (!reason.isEmpty() ? " Reason: " + reason : "");
        }
    }

    public record CanonicalizeTestCase(String id, String inputExpression, String factor, String canonicalForm) implements TestCase {}
}
