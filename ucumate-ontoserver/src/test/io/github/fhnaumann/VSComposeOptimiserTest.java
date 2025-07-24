package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.funcs.printer.UCUMSyntaxPrinter;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UcumVersion;
import io.github.fhnaumann.operations.ucum.VSComposeOptimiser;
import org.assertj.core.api.Assertions;
import org.assertj.core.presentation.StandardRepresentation;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Felix Naumann
 */
public class VSComposeOptimiserTest {

    private static final String CANONICAL_FILTER_PROPERTY = "canonical";
    private static final String PROPERTY_FILTER_PROPERTY = "property";

    public record Pair(ValueSet.ValueSetComposeComponent composeComponent, VSComposeOptimiser.OptimisationResult optimisationResult) {};

    private VSComposeOptimiser vsComposeOptimiser;

    public static class Display extends StandardRepresentation {

        private final Printer printer = new UCUMSyntaxPrinter();
        @Override
        public String toStringOf(Object object) {
            if(object instanceof UCUMExpression.Term term) {
                return printer.print(term);
            }
            /*
            if(object instanceof VSComposeOptimiser.OptimisedIncludeConcepts optimisedIncludeConcepts) {
                return optimisedIncludeConcepts.toString();
            }
            if(object instanceof VSComposeOptimiser.OptimisedCanonicalFilter optimisedCanonicalFilter) {
                return "include: %s\n operator: %s\n value: %s"
                        .formatted(optimisedCanonicalFilter.include(), optimisedCanonicalFilter.filterOperator(), optimisedCanonicalFilter.filterValue().stream().map(printer::print).toList().toString());
            }
            if(object instanceof VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts obj) {
                return "concepts: %s\n include: %s\n operator: %s\n value: %s"
                        .formatted(obj.explicitIncludeConcepts().toString(), obj.include(), obj.filterOperator(), obj.filterValue().stream().map(printer::print).toList().toString());
            }

             */
            return super.toStringOf(object);
        }
    }

    @BeforeEach
    public void setup() {
        vsComposeOptimiser = new VSComposeOptimiser(null, new UCUMService());
        Assertions.useRepresentation(new Display());
    }

    @ParameterizedTest
    @MethodSource("provide_optimisation_input")
    public void test(Pair pair) {
        VSComposeOptimiser.OptimisationResult actualResult = vsComposeOptimiser.optimise(pair.composeComponent);
        assert_result(actualResult, pair.optimisationResult);
    }

    private static void assert_result(VSComposeOptimiser.OptimisationResult actual, VSComposeOptimiser.OptimisationResult expected) {
        assertThat(actual.getClass()).isEqualTo(expected.getClass());
        switch (actual) {
            case VSComposeOptimiser.NotOptimised actualNotOptimised -> {}
            case VSComposeOptimiser.OptimisedIncludeConcepts actualOptimisedIncludeConcepts -> {
                VSComposeOptimiser.OptimisedIncludeConcepts expectedIncludeConcepts = (VSComposeOptimiser.OptimisedIncludeConcepts) expected;
                assertThat(actualOptimisedIncludeConcepts.explicitIncludeConcepts()).containsExactlyInAnyOrderElementsOf(expectedIncludeConcepts.explicitIncludeConcepts());
            }
            case VSComposeOptimiser.OptimisedCanonicalFilter actualOptimisedCanonicalFilter -> {
                VSComposeOptimiser.OptimisedCanonicalFilter expectedOptimisedCanonicalFilter = (VSComposeOptimiser.OptimisedCanonicalFilter) expected;
                assertThat(actualOptimisedCanonicalFilter.include()).isEqualTo(expectedOptimisedCanonicalFilter.include());
                assertThat(actualOptimisedCanonicalFilter.filterOperator()).isEqualTo(expectedOptimisedCanonicalFilter.filterOperator());
                assertThat(actualOptimisedCanonicalFilter.filterValue()).containsExactlyInAnyOrderElementsOf(expectedOptimisedCanonicalFilter.filterValue());
            }
            case VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts actual1 -> {
                VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts expected1 = (VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts) expected;
                assertThat(actual1.explicitIncludeConcepts()).containsExactlyInAnyOrderElementsOf(expected1.explicitIncludeConcepts());

                assertThat(actual1.include()).isEqualTo(expected1.include());
                assertThat(actual1.filterOperator()).isEqualTo(expected1.filterOperator());
                assertThat(actual1.filterValue()).containsExactlyInAnyOrderElementsOf(expected1.filterValue());
            }
        }
    }

    private static Stream<Pair> provide_optimisation_input() {
        return Stream.of(
                complex_case_1(),
                include_canonical_filter_complex(),
                include_property_filter(),
                include_filter_and_include_concept(),
                just_include_concept(),
                two_include_canonical_filters(),
                all_of_ucum_exclude_canonical_eq_filter(),
                single_include_canonical_eq_filter(),
                exclude_all_of_ucum(),
                all_of_ucum_no_exclude()
        );
    }

    private static Pair complex_case_1() {
        /*
        Include 1:
            Filter 1: "canonical = m.s"
            Filter 2: "canonical IN m.s.4,m,s,g"
            Filter 3: "canonical IN s.m"
        Include 2:
            Concept 1: "[ft_i]"
            Concept 2: "[ft_i].s.3"
        Include 3:
            Filter 1: "canonical IN 5.g,4.m.g"
            Filter 2: "canonical IN 3.g.m,10.g"
        Include 4:
            Concept 1: "[in_i]"
            Concept 2: "[yd_i]"
        Exclude 1:
            Filter 1: "canonical IN s.m,g"
            Filter 2: "canonical IN m.s,g"
        Exclude 2:
            Concept 1: "[yd_i]"
        Exclude 3:
            Filter 1: "property = time" (s)
        Exclude 4:
            Concept 1: "[in_i]"
            Concept 2: "N"
        ================================================ (take intersection)
        Include 1:
            Optimised Filter: "canonical = m.s"
        Include 2:
            Concept 1: "[ft_i]"
            Concept 2: "[ft_i].s.3"
        Include 3:
            Optimised Filter 1: "canonical IN g,m.g"
        Include 4:
            Concept 1: "[in_i]"
            Concept 2: "[yd_i]"
        Exclude 1:
            Optimised Filter: "canonical IN g,m.s"
        Exclude 2:
            Concept 1: "[yd_i]"
        Exclude 3:
            Optimised Filter: "canonical = s"
        Exclude 4:
            Concept 1: "[in_i]"
            Concept 2: "N"
        ================================================ (take union)
        Optimised Includes:
            Optimised Filter: "canonical IN g,m.s,m.g"
            Concepts: "[ft_i]", "[ft_i].s.3", "[in_i]", "[yd_i]"
        Optimised Excludes:
            Optimised Filter: "canonical IN s,g,m.s"
            Concepts: "[yd_i]", "[in_i]", "N"
        ================================================ (subtract include filter from exclude filter)
        Resulting Filter: "canonical = m.g"
        ================================================ (remove exclude concept from include concepts)
        Resulting include Concepts: "[ft_i]", "[ft_i].s.3"
        ================================================ (only keep include concepts IF they are NOT in the Optimised Exclude Filter)
        Resulting include Concepts: "[ft_i]"

        ===> Filter: "canonical = m.g" and the concept "[ft_i]"
         */
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        ValueSet.ConceptSetComponent include1 = composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        include1.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("m.s");
        include1.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("m.s.4,m,s,g");
        include1.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("s.m");
        ValueSet.ConceptSetComponent include2 = composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        include2.addConcept().setCode("[ft_i]");
        include2.addConcept().setCode("[ft_i].s.3");
        ValueSet.ConceptSetComponent include3 = composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        include3.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("5.g,4.m.g");
        include3.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("3.g.m,10.g");
        ValueSet.ConceptSetComponent include4 = composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        include4.addConcept().setCode("[in_i]");
        include4.addConcept().setCode("[yd_i]");
        ValueSet.ConceptSetComponent exclude1 = composeComponent.addExclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        exclude1.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("s.m,g");
        exclude1.addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("m.s,g");
        ValueSet.ConceptSetComponent exclude2 = composeComponent.addExclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        exclude2.addConcept().setCode("[yd_i]");
        ValueSet.ConceptSetComponent exclude3 = composeComponent.addExclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        exclude3.addFilter()
                .setProperty(PROPERTY_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("time");
        ValueSet.ConceptSetComponent exclude4 = composeComponent.addExclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        exclude4.addConcept().setCode("[in_i]");
        exclude4.addConcept().setCode("N");


        VSComposeOptimiser.OptimisationResult optimisationResult = new VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts(
                true,
                ValueSet.FilterOperator.EQUAL,
                of_c("m.g"),
                of("[ft_i]")
        );
        return new Pair(composeComponent, optimisationResult);
    }

    private static Pair include_canonical_filter_complex() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude()
                .setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("m.s");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedCanonicalFilter(
                true, ValueSet.FilterOperator.EQUAL, of_c("m.s")
        ));
    }

    private static Pair include_property_filter() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude()
                .setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(PROPERTY_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("length");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedCanonicalFilter(true, ValueSet.FilterOperator.EQUAL, of_c("m")));
    }

    private static Pair include_filter_and_include_concept() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude()
                .setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("g");
        composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM)
                .addConcept()
                .setCode("[ft_i]");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedCanonicalFilterAndIncludeConcepts(
                true, ValueSet.FilterOperator.EQUAL, of_c("g"), of("[ft_i]")
        ));
    }

    private static Pair just_include_concept() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM)
                .addConcept()
                .setCode("[ft_i]");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedIncludeConcepts(of("[ft_i]")));
    }

    private static Pair exclude_all_of_ucum() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addExclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        return new Pair(composeComponent, new VSComposeOptimiser.NotOptimised());
    }

    private static Pair two_include_canonical_filters() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude()
                .setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("m");

        composeComponent.addInclude()
                .setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.IN)
                .setValue("m,g");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedCanonicalFilter(true, ValueSet.FilterOperator.IN, of_c("m", "g")));
    }

    private static Pair all_of_ucum_exclude_canonical_eq_filter() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        composeComponent.addExclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("m");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedCanonicalFilter(false, ValueSet.FilterOperator.EQUAL, of_c("m")));
    }

    private static Pair all_of_ucum_no_exclude() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
        composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM);
        return new Pair(composeComponent, new VSComposeOptimiser.NotOptimised());
    }

    private static Pair single_include_canonical_eq_filter() {
        ValueSet.ValueSetComposeComponent  composeComponent = new ValueSet.ValueSetComposeComponent();
                composeComponent.addInclude().setSystem(UCUMOntoOperationPlugin.UCUM_SYSTEM).addFilter()
                .setProperty(CANONICAL_FILTER_PROPERTY)
                .setOp(ValueSet.FilterOperator.EQUAL)
                .setValue("m");
        return new Pair(composeComponent, new VSComposeOptimiser.OptimisedCanonicalFilter(true, ValueSet.FilterOperator.EQUAL, of_c("m")));
    }

    private static Set<UCUMExpression.Term> of(String... terms) {
        return Arrays.stream(terms)
                .map(s -> ((ValidatorService.Success)new Validator().validate(s)).term())
                .collect(Collectors.toSet());
    }

    private static Set<UCUMExpression.CanonicalTerm> of_c(String... terms) {
        return Arrays.stream(terms)
                .map(s -> Validator.parseCanonical(s, UcumVersion.V2_2))
                .collect(Collectors.toSet());
    }
}
