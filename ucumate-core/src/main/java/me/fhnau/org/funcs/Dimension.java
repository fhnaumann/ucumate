package me.fhnau.org.funcs;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Dimension {
    LENGTH,
    TIME,
    MASS,
    PLANE_ANGLE,
    TEMPERATURE,
    ELECTRIC_CHARGE,
    LUMINOUS_INTENSITY,
    NO_DIMENSION;

    public static Dimension fromUCUMEssenceString(String ucucmEssenceDimString) {
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

    static Map<Dimension, Integer> mergeDimensions(Map<Dimension, Integer> map1, Map<Dimension, Integer> map2) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, Integer::sum
                ));
    }

    static Map<Dimension, Integer> scaleDimensions(Map<Dimension, Integer> dimensions, int factor) {
        return dimensions.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, e -> e.getValue() * factor
                ));
    }

    static Map<Dimension, Integer> filterEmpty(Map<Dimension, Integer> map) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
