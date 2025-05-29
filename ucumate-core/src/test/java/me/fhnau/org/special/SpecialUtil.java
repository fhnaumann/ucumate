package me.fhnau.org.special;

import me.fhnau.org.UCUMDefinition;
import me.fhnau.org.builders.CombineTermBuilder;
import me.fhnau.org.builders.SoloTermBuilder;
import me.fhnau.org.funcs.Converter;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.util.PreciseDecimal;

import static me.fhnau.org.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SpecialUtil {

    private static final UCUMDefinition.UCUMUnit kelvin = getUCUMUnit("K");
    private static final UCUMDefinition.UCUMUnit celsius = getUCUMUnit("Cel");
    private static final UCUMDefinition.UCUMUnit degF = getUCUMUnit("[degF]");
    private static final UCUMDefinition.UCUMUnit prism_diopter = getUCUMUnit("[p'diop]");
    private static final UCUMDefinition.UCUMUnit percentage_slope = getUCUMUnit("%[slope]");
    private static final UCUMDefinition.UCUMUnit rad = getUCUMUnit("rad");
    private static final UCUMDefinition.UCUMUnit deg = getUCUMUnit("deg");
    private static final UCUMDefinition.UCUMUnit bel_volt = getUCUMUnit("B[V]");

    static UCUMExpression.Term rad_term() {
        return SoloTermBuilder.builder().withoutPrefix(rad).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term kelvin_term() {
        return SoloTermBuilder.builder().withoutPrefix(kelvin).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term _5_kelvin_term() {
        return CombineTermBuilder.builder().left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
            .multiplyWith()
            .right(kelvin_term())
            .build();
    }

    static UCUMExpression.Term celsius_term() {
        return SoloTermBuilder.builder().withoutPrefix(celsius).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term _5_celsius_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(celsius_term())
                                 .build();
    }

    static UCUMExpression.Term mega_celsius_term() {
        return SoloTermBuilder.builder().withPrefix(mega, celsius).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term _5_mega_celsius_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(mega_celsius_term())
                                 .build();
    }

    static UCUMExpression.Term degF_term() {
        return SoloTermBuilder.builder().withoutPrefix(degF).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term _5_degF_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(degF_term())
                                 .build();
    }

    static UCUMExpression.Term prism_diop_term() {
        return SoloTermBuilder.builder().withoutPrefix(prism_diopter).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term _5_prism_diop_term() {
        return CombineTermBuilder.builder()
                                 .left(SoloTermBuilder.builder().withIntegerUnit(5).noExpNoAnnot().asTerm().build())
                                 .multiplyWith()
                                 .right(prism_diop_term())
                                 .build();
    }

    static UCUMExpression.Term percentage_slope_term() {
        return SoloTermBuilder.builder().withoutPrefix(percentage_slope).noExpNoAnnot().asTerm().build();
    }

    static UCUMExpression.Term bel_volt_term() {
        return single(bel_volt);
    }

    static UCUMExpression.Term volt_term() {
        return single(getUCUMUnit("V"));
    }

    static UCUMExpression.Term degree_term() {
        return SoloTermBuilder.builder().withoutPrefix(deg).noExpNoAnnot().asTerm().build();
    }



    static void assert_success_and_equal_to(Converter.ConversionResult result, PreciseDecimal value) {
        assertThat(result)
                .isNotNull()
                .isInstanceOf(Converter.Success.class)
                .extracting(conversionResult -> ((Converter.Success) conversionResult).conversionFactor())
                .asString()
                .startsWith(value.toString());
    }
}
