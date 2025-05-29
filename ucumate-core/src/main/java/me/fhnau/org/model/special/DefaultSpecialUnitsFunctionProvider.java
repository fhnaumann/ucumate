package me.fhnau.org.model.special;

import me.fhnau.org.util.PreciseDecimal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DefaultSpecialUnitsFunctionProvider implements SpecialUnitsFunctionProvider {

    private static final Map<String, ConversionFunction> funcs = createFuncs();

    private static Map<String, ConversionFunction> createFuncs() {
        Map<String, ConversionFunction> tmpMap = new HashMap<>();
        tmpMap.put("Cel", of(
            number -> number - 273.15, // K to Cel
            number -> number + 273.15 // Cel to K
        ));
        tmpMap.put("degF", of(
                number -> number - 459.67, // K to [degF]
                number -> (number + 459.67) // [degF] to K
            )
        );
        tmpMap.put("degRe", of(
                number -> number - 218.52, // K to [degRe]
                number -> (number + 218.52) // [degRe] to K
            )
        );
        /*
        Probably a mistake in the spec. tanTimes100 seems to be equivalent to 100tan while it is never mentioned elsewhere in the spec.
         */
        tmpMap.put("tanTimes100", of(
                number -> Math.tan(number) * 100, // rad to prism diopter value
                number -> Math.atan(number / 100) // prims diopter value to rad
            )
        );
        tmpMap.put("100tan", tmpMap.get("tanTimes100"));
        tmpMap.put("hpX", negLogAnd10NegX());
        tmpMap.put("hpC", homeopathicPotency(100d));
        tmpMap.put("hpM", homeopathicPotency(1000d));
        tmpMap.put("hpQ", homeopathicPotency(50_000d));
        tmpMap.put("pH", negLogAnd10NegX());
        tmpMap.put("ln", of(
                Math::log,
                Math::exp
            )
        );
        tmpMap.put("lg", of(
                Math::log10,
                number -> Math.pow(10d, number)
            )
        );
        tmpMap.put("lgTimes2", of(
                number -> 2 * Math.log10(number),
                number -> Math.pow(10d, number / 2)
            )
        );
        tmpMap.put("sqrt", of(
            Math::sqrt,
            number -> Math.pow(number, 2)
        ));
        tmpMap.put("ld", of(
            number -> Math.log(number) / Math.log(2),
            number -> Math.pow(number, 2)
        ));
        return tmpMap;
    }

    private static ConversionFunction negLogAnd10NegX() {
        return of(
            number -> number != 0 ? -Math.log10(number) : 0,
            number -> Math.pow(10, -number)
        );
    }

    private static ConversionFunction homeopathicPotency(double  basis) {
        return of(
            number -> number != 0 ? (-Math.log(number) / Math.log(basis)) : 0,
            number -> Math.pow(basis, -number)
        );
    }

    private static ConversionFunction of(Function<Double, Double> fromCanonical, Function<Double, Double> toCanonical) {
        return new ConversionFunction() {
            @Override public PreciseDecimal fromCanonical(PreciseDecimal value) {
                return PreciseDecimal.fromDoubleFixedScale(fromCanonical.apply(value.getValue().doubleValue()));
            }

            @Override public PreciseDecimal toCanonical(PreciseDecimal value) {
                return PreciseDecimal.fromDoubleFixedScale(toCanonical.apply(value.getValue().doubleValue()));
            }
        };
    }

    @Override public Map<String, ConversionFunction> getConversionFuncs() {
        return Map.copyOf(funcs);
    }
}
