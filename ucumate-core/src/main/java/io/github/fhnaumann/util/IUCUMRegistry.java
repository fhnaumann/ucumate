package io.github.fhnaumann.util;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.model.UCUMExpression;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Felix Naumann
 */
public interface IUCUMRegistry {

    public List<UCUMDefinition.Concept> getAll();
    public Optional<UCUMDefinition.Concept> getConcept(String concept);
    public Collection<UCUMDefinition.UCUMPrefix> getPrefixes();
    public Collection<UCUMDefinition.BaseUnit> getBaseUnits();
    public Collection<UCUMDefinition.DefinedUnit> getDefinedUnits();
    public Optional<UCUMDefinition.UCUMPrefix> getPrefix(String prefix);
    public Optional<UCUMDefinition.BaseUnit> getBaseUnit(String baseUnit);
    public Optional<UCUMDefinition.DefinedUnit> getDefinedUnit(String definedUnit);
    public UCUMExpression.Term getDefinedUnitSourceDefinition(UCUMDefinition.DefinedUnit definedUnit, boolean enableMolarMassConversion);
    public Optional<UCUMDefinition.UCUMUnit> getUCUMUnit(String unit)M;

    public void warmup();

}
