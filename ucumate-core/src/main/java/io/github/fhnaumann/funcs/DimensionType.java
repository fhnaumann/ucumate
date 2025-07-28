package io.github.fhnaumann.funcs;

import io.github.fhnaumann.model.UCUMDefinition;
import io.github.fhnaumann.util.VersionSpecificUCUMRegistry;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DimensionType {
    LENGTH,
    TIME,
    MASS,
    PLANE_ANGLE,
    TEMPERATURE,
    ELECTRIC_CHARGE,
    LUMINOUS_INTENSITY,
    NO_DIMENSION;

    public static UCUMDefinition.BaseUnit getBaseUnit(DimensionType dimensionType, VersionSpecificUCUMRegistry ucumRegistry) {
        return ucumRegistry.getBaseUnits().stream()
                .filter(baseUnit -> fromUCUMEssenceString(baseUnit.dim()) == dimensionType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No base unit found for dim %s.".formatted(dimensionType)));
    }

    public static String getStringBaseUnit(DimensionType dimensionType, VersionSpecificUCUMRegistry ucumRegistry) {
        return getBaseUnit(dimensionType, ucumRegistry).code();
    }

    public static DimensionType fromUCUMEssenceString(String ucucmEssenceDimString) {
        return switch(ucucmEssenceDimString) {
            case "L" -> LENGTH;
            case "T" -> TIME;
            case "M" -> MASS;
            case "A" -> PLANE_ANGLE;
            case "C" -> TEMPERATURE;
            case "Q" -> ELECTRIC_CHARGE;
            case "F" -> LUMINOUS_INTENSITY;
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
