package me.fhnau.org.util;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtil {

    private static final Function<Boolean, Predicate<Map.Entry<?, Integer>>> filterEmptyIfDesired = aBoolean -> entry -> !aBoolean || entry.getValue() != 0;
    public static <K> Map<K, Integer> calculateDiff(Map<K,Integer> map1, Map<K,Integer> map2, boolean filterEmpty) {
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .distinct()
                .map(entry -> Map.entry(entry.getKey(), map1.getOrDefault(entry.getKey(),  0) - map2.getOrDefault(entry.getKey(), 0)))
            .distinct()
                .filter(filterEmptyIfDesired.apply(filterEmpty))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )
        );
    }


    public static <K,V extends Number> Map<K,V> filterEmpty(Map<K,V> map) {
        return map.entrySet().stream()
                .filter(kvEntry -> kvEntry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
