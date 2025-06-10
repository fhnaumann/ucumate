package io.github.fhnaumann;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.fhnaumann.model.special.SpecialUnitsFunctionProvider;
import io.github.fhnaumann.util.PreciseDecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PreciseSpecialUnitsFunctionProvider implements SpecialUnitsFunctionProvider {

    private static final MathContext mathContext = new MathContext(200);
    private static final Map<String, ConversionFunction> funcs = createFuncs();

    private static Map<String, ConversionFunction> createFuncs() {
        Map<String, ConversionFunction> tmpMap = new HashMap<>();
        tmpMap.put("Cel", SpecialUnitsFunctionProvider.of(
                preciseDecimal -> preciseDecimal.subtract(new PreciseDecimal("273.15")),
                preciseDecimal -> preciseDecimal.add(new PreciseDecimal("273.15"))
        ));
        tmpMap.put("degF", SpecialUnitsFunctionProvider.of(
                        preciseDecimal -> preciseDecimal.subtract(new PreciseDecimal("459.67")), // K to [degF]
                                preciseDecimal -> preciseDecimal.add(new PreciseDecimal("459.67") // [degF] to K
                )
        ));
        tmpMap.put("degRe", SpecialUnitsFunctionProvider.of(
                preciseDecimal -> preciseDecimal.subtract(new PreciseDecimal("218.52")), // K to [degF]
                preciseDecimal -> preciseDecimal.add(new PreciseDecimal("218.52") // [degF] to K
                )
        ));
        tmpMap.put("tanTimes100", ofBigDecimal(
                bigDecimal -> BigDecimalMath.tan(bigDecimal, mathContext).multiply(new BigDecimal("100"), mathContext),
                bigDecimal -> BigDecimalMath.atan(bigDecimal.divide(new BigDecimal("100"), mathContext), mathContext)
        ));
        tmpMap.put("100tan", tmpMap.get("tanTimes100"));
        tmpMap.put("hpX", negLogAnd10NegX());
        tmpMap.put("hpC", homeopathicPotency("100"));
        tmpMap.put("hpM", homeopathicPotency("1000"));
        tmpMap.put("hpQ", homeopathicPotency("50000"));
        tmpMap.put("pH", negLogAnd10NegX());
        tmpMap.put("ln", ofBigDecimal(
                bigDecimal -> BigDecimalMath.log(bigDecimal, mathContext),
                bigDecimal -> BigDecimalMath.exp(bigDecimal, mathContext)
        ));
        tmpMap.put("lg", ofBigDecimal(
                bigDecimal -> BigDecimalMath.log10(bigDecimal, mathContext),
                bigDecimal -> BigDecimalMath.pow(BigDecimal.TEN, bigDecimal, mathContext)
        ));
        tmpMap.put("lgTimes2", ofBigDecimal(
                bigDecimal -> BigDecimalMath.log10(bigDecimal, mathContext).multiply(BigDecimal.TWO, mathContext),
                bigDecimal -> BigDecimalMath.pow(BigDecimal.TEN, bigDecimal.divide(BigDecimal.TWO, mathContext), mathContext)
        ));
        tmpMap.put("sqrt", ofBigDecimal(
                bigDecimal -> BigDecimalMath.sqrt(bigDecimal, mathContext),
                bigDecimal -> BigDecimalMath.pow(bigDecimal, BigDecimal.TWO, mathContext)
        ));
        tmpMap.put("ld", ofBigDecimal(
                bigDecimal -> BigDecimalMath.log(bigDecimal, mathContext).divide(BigDecimalMath.log(BigDecimal.TWO, mathContext), mathContext),
                bigDecimal -> BigDecimalMath.pow(bigDecimal, BigDecimal.TWO, mathContext)
        ));
        return tmpMap;
    }

    private static ConversionFunction negLogAnd10NegX() {
        return ofBigDecimal(
                bigDecimal -> bigDecimal.compareTo(BigDecimal.ZERO) != 0 ? BigDecimalMath.log10(bigDecimal, mathContext).negate(mathContext) : BigDecimal.ZERO,
                bigDecimal -> BigDecimalMath.pow(BigDecimal.TEN, bigDecimal.negate(mathContext), mathContext)
        );
    }

    private static ConversionFunction homeopathicPotency(String basis) {
        return ofBigDecimal(
                bigDecimal -> bigDecimal.compareTo(BigDecimal.ZERO) != 0 ? (BigDecimalMath.log(bigDecimal, mathContext).negate(mathContext).divide(BigDecimalMath.log(new BigDecimal(basis), mathContext), mathContext)) : BigDecimal.ZERO,
                bigDecimal -> BigDecimalMath.pow(new BigDecimal(basis), bigDecimal.negate(mathContext), mathContext)
        );
    }

    @Override public Map<String, ConversionFunction> getConversionFuncs() {
        return Map.copyOf(funcs);
    }

    private static ConversionFunction ofBigDecimal(Function<BigDecimal, BigDecimal> fromCanonical, Function<BigDecimal, BigDecimal> toCanonical) {
        return new ConversionFunction() {
            @Override public PreciseDecimal fromCanonical(PreciseDecimal value) {
                return new PreciseDecimal(fromCanonical.apply(value.getValue()).toPlainString());
            }

            @Override public PreciseDecimal toCanonical(PreciseDecimal value) {
                return new PreciseDecimal(toCanonical.apply(value.getValue()).toPlainString());
            }
        };
    }
}
