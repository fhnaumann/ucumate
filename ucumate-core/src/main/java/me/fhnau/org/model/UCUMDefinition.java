package me.fhnau.org.model;

import me.fhnau.org.adapters.PreciseDecimalAdapter;
import me.fhnau.org.adapters.UCUMUnitAdapter;
import me.fhnau.org.util.PreciseDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Collection;
import java.util.List;

public sealed interface UCUMDefinition {

    @JacksonXmlRootElement(localName = "root", namespace = "http://unitsofmeasure.org/ucum-essence")
    record UCUMEssence(
            @JacksonXmlProperty(localName = "version", isAttribute = true) String version,
            @JacksonXmlProperty(localName = "revision", isAttribute = true) String revision,
            @JacksonXmlProperty(localName = "revision-date", isAttribute = true) String revisionDate,
            @JacksonXmlProperty(localName = "prefix") @JacksonXmlElementWrapper(useWrapping = false) List<UCUMPrefix> prefixes,
            @JacksonXmlProperty(localName = "base-unit") @JacksonXmlElementWrapper(useWrapping = false) List<BaseUnit> baseUnits,
            @JacksonXmlProperty(localName = "unit") @JacksonXmlElementWrapper(useWrapping = false) @JsonDeserialize(contentUsing = UCUMUnitAdapter.class) List<DefinedUnit> definedUnits
    ) { }

    sealed public interface Concept extends UCUMDefinition {
        String code();

        String codeCaseInsensitive();

        Collection<String> names();

        String printSymbol();
    }

    sealed interface UCUMUnit extends Concept {
        String property();
    }

    sealed public interface DefinedUnit extends UCUMUnit {

        boolean isMetric();

        UCUMValue value();
    }

    @JacksonXmlRootElement(localName = "base-unit")
    public record BaseUnit(
            @JacksonXmlProperty(localName = "Code", isAttribute = true) String code,
            @JacksonXmlProperty(localName = "CODE", isAttribute = true) String codeCaseInsensitive,
            @JacksonXmlProperty(localName = "dim", isAttribute = true) String dim,
            @JacksonXmlProperty(localName = "name") Collection<String> names,
            @JacksonXmlProperty(localName = "printSymbol") String printSymbol,
            @JacksonXmlProperty(localName = "property") String property
    ) implements UCUMUnit {
    }

    public record DerivedUnit(
            String code, String codeCaseInsensitive, boolean isMetric, String unitClass, Collection<String> names,
            String printSymbol, String property, UCUMValue value
    ) implements DefinedUnit {}

    public record DimlessUnit(
            String code, String codeCaseInsensitive, boolean isMetric, Collection<String> names, String printSymbol,
            String property, UCUMValue value
    ) implements DefinedUnit {
    }

    public record SpecialUnit(
            String code, String codeCaseInsensitive, boolean isMetric, String unitClass, Collection<String> names,
            String printSymbol, String property, UCUMValue value
    ) implements DefinedUnit {}

    public record ArbitraryUnit(
            String code, String codeCaseInsensitive, boolean isMetric, String unitClass, Collection<String> names,
            String printSymbol, String property, UCUMValue value
    ) implements DefinedUnit {}

    @JacksonXmlRootElement(localName = "unit")
    public record UnitDto(
            @JacksonXmlProperty(localName = "Code", isAttribute = true) String code,
            @JacksonXmlProperty(localName = "CODE", isAttribute = true) String codeAlt,
            @JacksonXmlProperty(localName = "isMetric", isAttribute = true) String isMetric,
            @JacksonXmlProperty(localName = "isSpecial", isAttribute = true) String isSpecial,
            @JacksonXmlProperty(localName = "isArbitrary", isAttribute = true) String isArbitrary,
            @JacksonXmlProperty(localName = "class", isAttribute = true) String unitClass,
            @JacksonXmlProperty(localName = "name") Collection<String> names,
            @JacksonXmlProperty(localName = "printSymbol") String printSymbol,
            @JacksonXmlProperty(localName = "property") String property,
            @JacksonXmlProperty(localName = "value") UCUMValue value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JacksonXmlRootElement(localName = "value")
    public record UCUMValue(
            @JacksonXmlProperty(localName = "Unit", isAttribute = true) String unit,
            @JacksonXmlProperty(localName = "UNIT", isAttribute = true) String unitAlt,
            @JacksonXmlProperty(localName = "value", isAttribute = true) @JsonDeserialize(using = PreciseDecimalAdapter.class) PreciseDecimal conversionFactor,
            @JacksonXmlProperty(localName = "function") UCUMFunction function
    ) {}

    @JacksonXmlRootElement(localName = "function")
    public record UCUMFunction(
            @JacksonXmlProperty(localName = "name", isAttribute = true) String name,
            @JacksonXmlProperty(localName = "value", isAttribute = true) @JsonDeserialize(using = PreciseDecimalAdapter.class) PreciseDecimal value,
            @JacksonXmlProperty(localName = "Unit", isAttribute = true) String unit
    ) {}

    @JacksonXmlRootElement(localName = "prefix")
    public record UCUMPrefix(
            @JacksonXmlProperty(localName = "Code", isAttribute = true) String code,
            @JacksonXmlProperty(localName = "CODE", isAttribute = true) String codeCaseInsensitive,
            @JacksonXmlElementWrapper(useWrapping = false) @JacksonXmlProperty(localName = "name") Collection<String> names,
            @JacksonXmlProperty(localName = "printSymbol") String printSymbol,
            @JacksonXmlProperty(localName = "value") UCUMValue value
    ) implements Concept {
    }
}
