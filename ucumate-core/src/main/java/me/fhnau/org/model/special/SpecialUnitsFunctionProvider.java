package me.fhnau.org.model.special;

import me.fhnau.org.util.PreciseDecimal;

import java.util.Map;
import java.util.function.Function;

public interface SpecialUnitsFunctionProvider {

    interface ConversionFunction {
        PreciseDecimal fromCanonical(PreciseDecimal value);
        PreciseDecimal toCanonical(PreciseDecimal value);
    }

    Map<String, ConversionFunction> getConversionFuncs();

    default ConversionFunction getFunction(String name) {
        ConversionFunction conversionFunction = getConversionFuncs().get(name);
        if(conversionFunction == null) {
            throw new IllegalArgumentException("%s is not a known special function!".formatted(name));
        }
        return conversionFunction;
    }

    static ConversionFunction of(Function<PreciseDecimal, PreciseDecimal> fromCanonical, Function<PreciseDecimal, PreciseDecimal> toCanonical) {
        return new ConversionFunction() {
            @Override public PreciseDecimal fromCanonical(PreciseDecimal value) {
                return fromCanonical.apply(value);
            }

            @Override public PreciseDecimal toCanonical(PreciseDecimal value) {
                return toCanonical.apply(value);
            }
        };
    }
}
