package org.example.model;

import org.example.util.PreciseDecimal;

import java.util.Map;

public interface SpecialUnitsFunctionProvider {

    Map<String, ConversionFunction> getFunctions();

    public interface ForwardConversion {
        PreciseDecimal convert(PreciseDecimal number);
    }

    public interface InverseConversion {
        PreciseDecimal inverse(PreciseDecimal number);
    }

    public interface ConversionFunction {

        ForwardConversion forwardConversion();

        default PreciseDecimal convert(PreciseDecimal number) {
            return forwardConversion().convert(number);
        }

        InverseConversion inverseConversion();

        default PreciseDecimal inverse(PreciseDecimal number) {
            return inverseConversion().inverse(number);
        }

        static ConversionFunction of(ForwardConversion forwardConversion, InverseConversion inverseConversion) {

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
