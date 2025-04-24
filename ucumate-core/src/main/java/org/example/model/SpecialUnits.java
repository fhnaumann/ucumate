package org.example.model;

import java.util.HashMap;
import java.util.Map;

public class SpecialUnits {

    private static final Map<String, ConversionFunction> functionNamesToFuncs;

    static {
        Map<String, ConversionFunction> tmpMap = new HashMap<>();

        tmpMap.put("Cel", ConversionFunction.of(
                           number -> number - 273.15, // K to Cel
                           number -> number + 273.15 // Cel to K
                   )
        );
        tmpMap.put("degF", ConversionFunction.of(
                           number -> 9d / 5d * number - 459.67, // K to [degF]
                           number -> 5d / 9d * (number + 459.67) // [degF] to K
                   )
        );
        tmpMap.put("degRe", ConversionFunction.of(
                           number -> 4d / 5d * number - 218.52, // K to [degRe]
                           number -> 5d / 4d * (number + 218.52) // [degRe] to K
                   )
        );
        /*
        Probably a mistake in the spec. tanTimes100 seems to be equivalent to 100tan while it is never mentioned elsewhere in the spec.
         */
        tmpMap.put("tanTimes100", ConversionFunction.of(
                           number -> Math.tan(number) * 100, // rad to prism diopter value
                           number -> Math.atan(number / 100) // prims diopter value to rad
                   )
        );
        tmpMap.put("100tan", ConversionFunction.of(
                           number -> Math.tan(number) * 100,
                           number -> Math.atan(number / 100)
                   )
        );
        tmpMap.put("hpX", negLogAnd10NegX());
        tmpMap.put("hpC", homeopathicPotency(100d));
        tmpMap.put("hpM", homeopathicPotency(1000d));
        tmpMap.put("hpQ", homeopathicPotency(50_000d));
        tmpMap.put("pH", negLogAnd10NegX());
        tmpMap.put("ln", ConversionFunction.of(
                           Math::log,
                           Math::exp
                   )
        );
        tmpMap.put("lg", ConversionFunction.of(
                           Math::log10,
                           number -> Math.pow(10d, number)
                   )
        );
        tmpMap.put("lgTimes2", ConversionFunction.of(
                           number -> 2 * Math.log10(number),
                           number -> Math.pow(10d, number / 2)
                   )
        );
        tmpMap.put("sqrt", ConversionFunction.of(
                Math::sqrt,
                number -> Math.pow(number, 2)
        ));
        tmpMap.put("ld", ConversionFunction.of(
                number -> Math.log(number) / Math.log(2),
                number -> Math.pow(number, 2)
        ));
        // TODO: Math.log(x)/Math.log(y) may lead to imprecise results
        // https://stackoverflow.com/a/3305710/15158714

        functionNamesToFuncs = Map.copyOf(tmpMap);
    }

    private static ConversionFunction negLogAnd10NegX() {
        return ConversionFunction.of(
                Math::log10,
                number -> Math.pow(10, -number)
        );
    }

    private static ConversionFunction homeopathicPotency(double basis) {
        return ConversionFunction.of(
                number -> Math.log(number) / Math.log(basis),
                number -> Math.pow(basis, -number)
        );
    }

    public static ConversionFunction getFunction(String functionName) {
        return functionNamesToFuncs.get(functionName);
    }

    public interface ForwardConversion {
        double convert(double number);
    }

    public interface InverseConversion {
        double inverse(double number);
    }

    public interface ConversionFunction {

        ForwardConversion forwardConversion();

        default double convert(double number) {
            return forwardConversion().convert(number);
        }

        InverseConversion inverseConversion();

        default double inverse(double number) {
            return inverseConversion().inverse(number);
        }

        private static ConversionFunction of(ForwardConversion forwardConversion, InverseConversion inverseConversion) {

            return new ConversionFunction() {
                @Override
                public ForwardConversion forwardConversion() {
                    return forwardConversion;
                }

                @Override
                public InverseConversion inverseConversion() {
                    return inverseConversion;
                }
            };
        }
    }
}
