package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dimension of a canonical term. This is either one of the UCUM base dimensions or an arbitrary dimension.
 */
public sealed interface DimensionType permits DimensionType.BaseDimension, DimensionType.ArbitraryDimension {

    /**
     * The dimensions of the UCUM base units.
     */
    enum BaseDimension implements DimensionType {
        LENGTH,
        TIME,
        MASS,
        PLANE_ANGLE,
        TEMPERATURE,
        ELECTRIC_CHARGE,
        LUMINOUS_INTENSITY,
        NO_DIMENSION
    }

    /**
     * Each (terminal) arbitrary unit is its own dimension, i.e. <code>[iU]</code> and <code>[arb'U]</code> are two
     * different dimensions which are also different from all base dimensions.
     * @param code The code of the arbitrary unit.
     */
    record ArbitraryDimension(String code) implements DimensionType {
        @Override
        public String toString() {
            return code;
        }
    }

    static UCUMDefinition.BaseUnit getBaseUnit(BaseDimension dimensionType, VersionSpecificUCUMRegistry ucumRegistry) {
        return ucumRegistry.getBaseUnits().stream()
                .filter(baseUnit -> fromUCUMEssenceString(baseUnit.dim()) == dimensionType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No base unit found for dim %s.".formatted(dimensionType)));
    }

    static String getStringBaseUnit(BaseDimension dimensionType, VersionSpecificUCUMRegistry ucumRegistry) {
        return getBaseUnit(dimensionType, ucumRegistry).code();
    }

    static BaseDimension fromUCUMEssenceString(String ucucmEssenceDimString) {
        return switch(ucucmEssenceDimString) {
            case "L" -> BaseDimension.LENGTH;
            case "T" -> BaseDimension.TIME;
            case "M" -> BaseDimension.MASS;
            case "A" -> BaseDimension.PLANE_ANGLE;
            case "C" -> BaseDimension.TEMPERATURE;
            case "Q" -> BaseDimension.ELECTRIC_CHARGE;
            case "F" -> BaseDimension.LUMINOUS_INTENSITY;
            default -> throw new IllegalArgumentException("Unknown UCUM Essence Dimension %s".formatted(ucucmEssenceDimString));
        };
    }

    static Map<DimensionType, Integer> mergeDimensions(Map<DimensionType, Integer> map1, Map<DimensionType, Integer> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, Integer::sum
                ));
    }

    static Map<DimensionType, Integer> scaleDimensions(Map<DimensionType, Integer> dimensions, int factor) {
        return dimensions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, e -> e.getValue() * factor
                ));
    }

    static Map<DimensionType, Integer> filterEmpty(Map<DimensionType, Integer> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
