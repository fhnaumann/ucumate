package org.example.special;

import org.example.UCUMDefinition;
import org.example.UCUMRegistry;
import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.funcs.Converter;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.TestUtil.*;

public class SpecialUtil {

    private static final UCUMDefinition.UCUMUnit kelvin = getUCUMUnit("K");
    private static final UCUMDefinition.UCUMUnit celsius = getUCUMUnit("Cel");
    private static final UCUMDefinition.UCUMUnit degF = getUCUMUnit("[degF]");
    private static final UCUMDefinition.UCUMUnit prism_diopter = getUCUMUnit("[p'diop]");
    private static final UCUMDefinition.UCUMUnit percentage_slope = getUCUMUnit("%[slope]");
    private static final UCUMDefinition.UCUMUnit rad = getUCUMUnit("rad");
    private static final UCUMDefinition.UCUMUnit deg = getUCUMUnit("deg");

    static Expression.Term rad_term() {
        return SoloTermBuilder.builder().withoutPrefix(rad).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term kelvin_term() {
        return SoloTermBuilder.builder().withoutPrefix(kelvin).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term celsius_term() {
        return SoloTermBuilder.builder().withoutPrefix(celsius).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term _5_celsius_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(celsius_term())
                                 .build();
    }

    static Expression.Term mega_celsius_term() {
        return SoloTermBuilder.builder().withPrefix(mega, celsius).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term _5_mega_celsius_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(mega_celsius_term())
                                 .build();
    }

    static Expression.Term degF_term() {
        return SoloTermBuilder.builder().withoutPrefix(degF).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term _5_degF_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(degF_term())
                                 .build();
    }

    static Expression.Term prism_diop_term() {
        return SoloTermBuilder.builder().withoutPrefix(prism_diopter).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term _5_prism_diop_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(prism_diop_term())
                                 .build();
    }

    static Expression.Term percentage_slope_term() {
        return SoloTermBuilder.builder().withoutPrefix(percentage_slope).noExpNoAnnot().asTerm().build();
    }

    static Expression.Term degree_term() {
        return SoloTermBuilder.builder().withoutPrefix(deg).noExpNoAnnot().asTerm().build();
    }

    static void assert_success_and_equal_to(Converter.ConversionResult result, PreciseDecimal value) {
        assertThat(result)
                .isNotNull()
                .isInstanceOf(Converter.Success.class)
                .extracting(conversionResult -> ((Converter.Success) conversionResult).conversionFactor())
                .isEqualTo(value);
    }
}
