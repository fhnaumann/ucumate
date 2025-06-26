package io.github.fhnaumann.operations.ucum.filters;

import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.operations.ucum.InvalidInputException;
import org.hl7.fhir.r4.model.ValueSet;

import java.util.Collection;
import java.util.List;

/**
 * @author Felix Naumann
 */
public interface ApplyFilter {

    Collection<UCUMExpression.Term> apply(String expression, ValueSet.FilterOperator operator) throws InvalidInputException;
}
