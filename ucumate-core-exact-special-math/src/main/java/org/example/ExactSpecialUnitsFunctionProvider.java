package org.example;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.example.model.SpecialUnits;
import org.example.model.SpecialUnitsFunctionProvider;
import org.example.util.PreciseDecimal;

import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

public class ExactSpecialUnitsFunctionProvider implements SpecialUnitsFunctionProvider {

    private static final MathContext CTX = new MathContext(100);

    private static final Map<String, SpecialUnitsFunctionProvider.ConversionFunction> functionNamesToFuncs;

    static {
        Map<String, SpecialUnitsFunctionProvider.ConversionFunction> tmpMap = new HashMap<>();

        tmpMap.put("Cel", SpecialUnitsFunctionProvider.ConversionFunction.of(
                number -> number.subtract(pd("273.15")),
                number -> number.add(pd("273.15"))
        ));
        tmpMap.put("degF", SpecialUnitsFunctionProvider.ConversionFunction.of(
                           number -> pd("9").divide(pd("5")).multiply(number).subtract(pd("459.67")), // K to [degF]
                           number -> pd("5").divide(pd("9")).multiply(number.add(pd("459.67"))) // [degF] to K
                   )
        );
        tmpMap.put("degRe", SpecialUnitsFunctionProvider.ConversionFunction.of(
                number -> pd("4").divide(pd("5")).multiply(number).subtract(pd("218.52")),
                number -> pd("5").divide(pd("4")).multiply(number.add(pd("218.52")))
        ));
        tmpMap.put("tanTimes100", SpecialUnitsFunctionProvider.ConversionFunction.of(
                number -> pd(BigDecimalMath.tan(number.getValue(), CTX).toPlainString()).multiply(pd("100")),
                number -> pd(BigDecimalMath.atan(number.divide(pd("100")).getValue(), CTX).toPlainString())
        ));
        tmpMap.put("100tan", tmpMap.get("tanTimes100"));

        functionNamesToFuncs = Map.copyOf(tmpMap);
    }

    @Override
    public Map<String, ConversionFunction> getFunctions() {
        return functionNamesToFuncs;
    }

    private static PreciseDecimal pd(String s) {
        return new PreciseDecimal(s);
    }
}
