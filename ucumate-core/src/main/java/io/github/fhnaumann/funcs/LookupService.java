package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;

import java.util.List;

/**
 * @author Felix Naumann
 */
public interface LookupService {

    public LookupResult lookup(String input);

    public sealed interface LookupResult {}
    public sealed interface Success extends LookupResult {}
    public record DirectMatch(UCUMDefinition.UCUMUnit unit) implements Success {}
    public record MultipleMatches(List<UCUMDefinition.UCUMUnit> units) implements Success {}
    public record Failure() implements LookupResult {}
}
