package io.github.fhnaumann;

import ca.uhn.fhir.context.FhirContext;
import io.github.fhnaumann.builders.CacheConfig;
import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.operations.ExpandOperation;
import io.github.fhnaumann.operations.ucum.UCUMExpandOperation;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class ExpandOperationTest {

    public static String URI = "http://unitsofmeasure.org";

    private ExpandOperation plugin;

    @BeforeEach
    public void setup() {
        PersistenceRegistry.disableInMemoryCache(true);
        PersistenceRegistry.initCache(CacheConfig.builder().enable().preHeat(true).build());
        plugin = new UCUMExpandOperation(new UCUMService());
    }

    @Test
    public void test_expand_code_operation_explicit_include() {
        ValueSet vs = create_include_explicit_codes_vs(null, "m", "cm", "[ft_i]");
        ValueSet expanded = perform_expand(plugin, vs, null);

        assert_expansion_contains_codes(expanded, "m", "cm", "[ft_i]");
    }

    @Test
    public void test_expand_code_operation_filter_implicit_include() {
        ValueSet vs = create_include_canonical_filter_implicit_codes_vs(null, "m", true);
        ValueSet actualExpanded = perform_expand(plugin, vs, "foot");
        assert_expansion_contains_codes(actualExpanded, "[ft_br]", "[ft_i]", "[ft_us]");
    }

    @Test
    public void test_expand_code_operation_explicit_include_and_filter_implicit_include() {
        ValueSet vs = create_include_explicit_codes_vs(null, "[sft_i]"); // square foot canonicalizes to m2 (not m) AND matches the "foot" text filter
        vs = create_include_canonical_filter_implicit_codes_vs(vs, "m", true);
        ValueSet actualExpanded = perform_expand(plugin, vs, "foot");
        assert_expansion_contains_codes(actualExpanded, "[sft_i]", "[ft_br]", "[ft_i]", "[ft_us]");
    }

    @Test
    public void test_filters_on_same_include_are_intersected() {
        ValueSet vs = create_include_canonical_filter_implicit_codes_vs(null, "m", true);
        vs = create_include_canonical_filter_implicit_codes_vs(vs, "[ft_i]", false);
        ValueSet actualExpanded = perform_expand(plugin, vs, "foot");
        assert_expansion_contains_codes(actualExpanded, "[ft_br]", "[ft_i]", "[ft_us]");
    }

    @Test
    public void test_property_filter_returns_matches() {
        ValueSet vs = create_include_property_filter_implicit_codes_vs(null, "length", false);
        ValueSet actualExpanded = perform_expand(plugin, vs, "foot");
        assert_expansion_contains_codes(actualExpanded, "[ft_br]", "[ft_i]", "[ft_us]");
    }

    @Test
    public void test_parsing_error_returns_operation_outcome_with_code_invalid() {
        ValueSet vs = create_include_explicit_codes_vs(null, "m", "not_a_real_code", "s");
        OperationOutcome actualOperationOutcome = perform_invalid_expand(plugin, vs, null);
        assert_operation_outcome(actualOperationOutcome, OperationOutcome.IssueType.CODEINVALID);
    }

    @Test
    public void test_wrong_operator_in_canonical_filter_returns_operation_outcome_with_not_supported() {
        ValueSet vs = create_include_canonical_filter_implicit_codes_vs(null, "m", false);
        // manually override test setup to create invalid scenario
        vs.getCompose().getIncludeFirstRep().getFilterFirstRep().setOp(ValueSet.FilterOperator.EXISTS);
        OperationOutcome actualOperationOutcome = perform_invalid_expand(plugin, vs, null);
        assert_operation_outcome(actualOperationOutcome, OperationOutcome.IssueType.NOTSUPPORTED);
    }

    @Test
    public void test_wrong_operator_in_property_filter_returns_operation_outcome_with_not_supported() {
        ValueSet vs = create_include_property_filter_implicit_codes_vs(null, "mass", false);
        // manually override test setup to create invalid scenario
        vs.getCompose().getIncludeFirstRep().getFilterFirstRep().setOp(ValueSet.FilterOperator.EXISTS);
        OperationOutcome actualOperationOutcome = perform_invalid_expand(plugin, vs, null);
        assert_operation_outcome(actualOperationOutcome, OperationOutcome.IssueType.NOTSUPPORTED);
    }

    @Test
    public void test_wrong_property_name_in_property_filter_returns_operation_outcome_with_not_supported() {
        ValueSet vs = create_include_property_filter_implicit_codes_vs(null, "not_a_property", false);
        OperationOutcome actualOperationOutcome = perform_invalid_expand(plugin, vs, null);
        assert_operation_outcome(actualOperationOutcome, OperationOutcome.IssueType.NOTSUPPORTED);
    }

    @Test
    @Disabled("too many codes returned")
    public void test_expand_code_operation_returns_all_known_codes() {
        ValueSet vs = new ValueSet();
        ValueSet actualExpanded = perform_expand(plugin, vs, null);
        assert_expansion_contains_codes(actualExpanded, "TODO");
    }

    private static void assert_operation_outcome(OperationOutcome actual, OperationOutcome.IssueType expectedIssueType) {
        assertThat(actual.hasIssue()).isTrue();
        assertThat(actual.getIssue().size()).isEqualTo(1);
        assertThat(actual.getIssueFirstRep().getCode()).isEqualTo(expectedIssueType);
    }

    private static void print_json(ValueSet valueSet) {
        String print = FhirContext.forR4().newJsonParser().setPrettyPrint(true).encodeResourceToString(valueSet);
        System.out.println(print);
    }

    private static OperationOutcome perform_invalid_expand(ExpandOperation plugin, ValueSet valueSet, String textFilter) {
        return ((ExpandOperation.Failure) plugin.expand(valueSet, textFilter)).outcome();
    }

    private static ValueSet perform_expand(ExpandOperation plugin, ValueSet valueSet, String textFilter) {
        return ((ExpandOperation.Success) plugin.expand(valueSet, textFilter)).valueSet();
    }

    private static void assert_expansion_contains_codes(ValueSet actualExpanded, String... expectedExpandedCodes) {
        List<String> actualExpandedCodes = actualExpanded.getExpansion().getContains().stream()
                .map(ValueSet.ValueSetExpansionContainsComponent::getCode)
                .toList();
        assertThat(actualExpandedCodes).containsExactlyInAnyOrderElementsOf(Arrays.asList(expectedExpandedCodes));
    }

    private static ValueSet create_include_property_filter_implicit_codes_vs(ValueSet existing, String propertyName, boolean newInclude) {
        ValueSet valueSet = existing != null ? existing : basic();
        addPropertyFilter(valueSet, propertyName, newInclude);
        return valueSet;
    }

    private static ValueSet create_include_canonical_filter_implicit_codes_vs(ValueSet existing, String code, boolean newInclude) {
        ValueSet valueSet = existing != null ? existing : basic();
        addCanonicalFilter(valueSet, code, newInclude);
        return valueSet;
    }

    private static ValueSet create_include_explicit_codes_vs(ValueSet existing, String... codes) {
        ValueSet valueSet = existing != null ? existing : basic();
        addExplicitCodes(valueSet, codes);
        return valueSet;
    }

    private static void addExplicitCodes(ValueSet valueSet, String... codes) {
        ValueSet.ConceptSetComponent comp = valueSet.getCompose().addInclude();
        if(codes != null) {
            for(String code : codes) {
                comp.addConcept().setCode(code);
            }
        }
    }

    private static void addCanonicalFilter(ValueSet valueSet, String code, boolean newInclude) {
        ValueSet.ConceptSetFilterComponent f = new ValueSet.ConceptSetFilterComponent();
        f.setProperty("canonical")
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue(code);
        if(newInclude) {
            valueSet.getCompose().addInclude().addFilter(f);
        }
        else {
            valueSet.getCompose().getIncludeFirstRep().addFilter(f);
        }
    }

    private static void addPropertyFilter(ValueSet valueSet, String propertyName, boolean newInclude) {
        ValueSet.ConceptSetFilterComponent f = new ValueSet.ConceptSetFilterComponent();
        f.setProperty("property")
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue(propertyName);
        if(newInclude) {
            valueSet.getCompose().addInclude().addFilter(f);
        }
        else {
            valueSet.getCompose().getIncludeFirstRep().addFilter(f);
        }
    }

    private static ValueSet basic() {
        ValueSet valueSet = new ValueSet();
        valueSet.setUrl(URI);
        valueSet.setVersion(UcumVersion.V2_2.getVersion());
        return valueSet;
    }
}
