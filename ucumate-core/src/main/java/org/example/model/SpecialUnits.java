package org.example.model;

import org.example.util.PreciseDecimal;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class SpecialUnits {

    private static final Map<String, SpecialUnitsFunctionProvider.ConversionFunction> functionNamesToFuncs;

    static {
        Map<String, SpecialUnitsFunctionProvider.ConversionFunction> tmpMap = new HashMap<>();

        tmpMap.put("Cel", SimpleConversionFunction.of(
                           number -> number - 273.15, // K to Cel
                           number -> number + 273.15 // Cel to K
                   )
        );
        tmpMap.put("degF", SimpleConversionFunction.of(
                           number -> 9d / 5d * number - 459.67, // K to [degF]
                           number -> 5d / 9d * (number + 459.67) // [degF] to K
                   )
        );
        tmpMap.put("degRe", SimpleConversionFunction.of(
                           number -> 4d / 5d * number - 218.52, // K to [degRe]
                           number -> 5d / 4d * (number + 218.52) // [degRe] to K
                   )
        );
        /*
        Probably a mistake in the spec. tanTimes100 seems to be equivalent to 100tan while it is never mentioned elsewhere in the spec.
         */
        tmpMap.put("tanTimes100", SimpleConversionFunction.of(
                           number -> Math.tan(number) * 100, // rad to prism diopter value
                           number -> Math.atan(number / 100) // prims diopter value to rad
                   )
        );
        tmpMap.put("100tan", SimpleConversionFunction.of(
                           number -> Math.tan(number) * 100,
                           number -> Math.atan(number / 100)
                   )
        );
        tmpMap.put("hpX", negLogAnd10NegX());
        tmpMap.put("hpC", homeopathicPotency(100d));
        tmpMap.put("hpM", homeopathicPotency(1000d));
        tmpMap.put("hpQ", homeopathicPotency(50_000d));
        tmpMap.put("pH", negLogAnd10NegX());
        tmpMap.put("ln", SimpleConversionFunction.of(
                           Math::log,
                           Math::exp
                   )
        );
        tmpMap.put("lg", SimpleConversionFunction.of(
                           Math::log10,
                           number -> Math.pow(10d, number)
                   )
        );
        tmpMap.put("lgTimes2", SimpleConversionFunction.of(
                           number -> 2 * Math.log10(number),
                           number -> Math.pow(10d, number / 2)
                   )
        );
        tmpMap.put("sqrt", SimpleConversionFunction.of(
                Math::sqrt,
                number -> Math.pow(number, 2)
        ));
        tmpMap.put("ld", SimpleConversionFunction.of(
                number -> Math.log(number) / Math.log(2),
                number -> Math.pow(number, 2)
        ));
        // TODO: Math.log(x)/Math.log(y) may lead to imprecise results
        // https://stackoverflow.com/a/3305710/15158714

        ServiceLoader<SpecialUnitsFunctionProvider> loader = ServiceLoader.load(SpecialUnitsFunctionProvider.class);
        if(loader.findFirst().isPresent()) {
            System.out.println("do better logging: Loaded SpecialUnitsFunctionProvider implementation!");
        }
        loader.forEach(provider -> tmpMap.putAll(provider.getFunctions()));

        functionNamesToFuncs = Map.copyOf(tmpMap);
    }

    private static SimpleConversionFunction negLogAnd10NegX() {
        return SimpleConversionFunction.of(
                Math::log10,
                number -> Math.pow(10, -number)
        );
    }

    private static SimpleConversionFunction homeopathicPotency(double basis) {
        return SimpleConversionFunction.of(
                number -> Math.log(number) / Math.log(basis),
                number -> Math.pow(basis, -number)
        );
    }

    public static SpecialUnitsFunctionProvider.ConversionFunction getFunction(String functionName) {
        return functionNamesToFuncs.get(functionName);
    }


    public interface SimpleForwardConversion extends SpecialUnitsFunctionProvider.ForwardConversion {
        double convert(double number);

        @Override
        default PreciseDecimal convert(PreciseDecimal number) {
            return PreciseDecimal.fromDoubleFixedScale(convert(number.getValue().doubleValue()));
        }
    }

    public interface SimpleInverseConversion extends SpecialUnitsFunctionProvider.InverseConversion {
        double invert(double number);

        @Override
        default PreciseDecimal inverse(PreciseDecimal number) {
            return PreciseDecimal.fromDoubleFixedScale(invert(number.getValue().doubleValue()));
        }
    }

    public interface SimpleConversionFunction extends SpecialUnitsFunctionProvider.ConversionFunction {

        default double convert(double number) {
            return forwardConversion().convert(number);
        }

        default double inverse(double number) {
            return inverseConversion().invert(number);
        }

        @Override
        SimpleForwardConversion forwardConversion();

        @Override
        SimpleInverseConversion inverseConversion();

        static SimpleConversionFunction of(SimpleForwardConversion forwardConversion, SimpleInverseConversion inverseConversion) {

            return new SimpleConversionFunction() {
                @Override
                public SimpleForwardConversion forwardConversion() {
                    return forwardConversion;
                }

                @Override
                public SimpleInverseConversion inverseConversion() {
                    return inverseConversion;
                }
            };
        }
    }
}
