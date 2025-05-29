package me.fhnau.org.funcs;

import me.fhnau.org.builders.SoloTermBuilder;
import me.fhnau.org.model.UCUMExpression;

public class Defaulter {
    public static final UCUMExpression.Exponent DEFAULT_EXPONENT = new UCUMExpression.Exponent(0);
    public static final UCUMExpression.Annotation DEFAULT_ANNOTATION = new UCUMExpression.Annotation("");
    public static final UCUMExpression.Term DEFAULT_TERM = SoloTermBuilder.builder()
                                                                      .withIntegerUnit(1)
                                                                      .noExpNoAnnot()
                                                                      .asTerm()
                                                                      .build();
}
