package io.github.fhnaumann.configuration;

/**
 * @author Felix Naumann
 */
import io.github.fhnaumann.model.UCUMExpression;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FeatureFlags {

    private static final String DELIMITER = "::cfg=";

    public enum Flag {
        PREFIX_ON_NON_METRIC,
        MOL_MASS_CONVERSION,
        ANNOT_AFTER_PARENS;
    }

    private final EnumSet<Flag> flags;
    private final int bitmask;

    private static final ConcurrentMap<Integer, FeatureFlags> INTERNED = new ConcurrentHashMap<>();

    private FeatureFlags(EnumSet<Flag> flags) {
        this.flags = EnumSet.copyOf(flags);
        this.bitmask = computeBitmask(flags);
    }

    public static FeatureFlags of(Set<Flag> flags) {
        int mask = computeBitmask(flags);
        return INTERNED.computeIfAbsent(mask, m -> new FeatureFlags(EnumSet.copyOf(flags)));
    }

    public static FeatureFlags of(Flag... flags) {
        return of(EnumSet.of(flags[0], flags));
    }

    public static FeatureFlags none() {
        return of(EnumSet.noneOf(Flag.class));
    }

    public static FeatureFlags all() {
        return of(EnumSet.allOf(Flag.class));
    }

    public FeatureFlags with(Flag flag) {
        if (flags.contains(flag)) return this;
        EnumSet<Flag> newFlags = EnumSet.copyOf(flags);
        newFlags.add(flag);
        return of(newFlags);
    }

    public FeatureFlags without(Flag flag) {
        if (!flags.contains(flag)) return this;
        EnumSet<Flag> newFlags = EnumSet.copyOf(flags);
        newFlags.remove(flag);
        return of(newFlags);
    }

    public boolean isEnabled(Flag flag) {
        return flags.contains(flag);
    }

    public Set<Flag> asSet() {
        return Collections.unmodifiableSet(flags);
    }

    private static int computeBitmask(Set<Flag> flags) {
        int mask = 0;
        for (Flag flag : flags) {
            mask |= (1 << flag.ordinal());
        }
        return mask;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FeatureFlags other && this.bitmask == other.bitmask;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(bitmask);
    }

    @Override
    public String toString() {
        return "FeatureFlags" + flags;
    }

    public static String toStorageKey(String expression, FeatureFlags flags) {
        return expression + DELIMITER + flags.bitmask;
    }

    public static ValKey fromStorageKey(String storageKey) {
        int idx = storageKey.indexOf(DELIMITER);
        if (idx == -1) {
            throw new IllegalArgumentException("Invalid storage key format: " + storageKey);
        }
        String expr = storageKey.substring(0, idx);
        int bitmask = Integer.parseInt(storageKey.substring(idx + DELIMITER.length()));
        return new ValKey(expr, fromBitmask(bitmask));
    }

    private static FeatureFlags fromBitmask(int bitmask) {
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        for (Flag flag : Flag.values()) {
            if ((bitmask & (1 << flag.ordinal())) != 0) {
                flags.add(flag);
            }
        }
        return of(flags);
    }

}
