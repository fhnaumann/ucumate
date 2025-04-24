package org.example.funcs;

import org.example.UCUMDefinition;
import org.example.builders.SoloTermBuilder;
import org.example.model.Expression;

import java.util.List;

public class Defaulter {
    public static final Expression.Exponent DEFAULT_EXPONENT = new Expression.Exponent(0);
    public static final Expression.Annotation DEFAULT_ANNOTATION = new Expression.Annotation("");
    public static final Expression.Term DEFAULT_TERM = SoloTermBuilder.builder()
                                                                      .withIntegerUnit(1)
                                                                      .noExpNoAnnot()
                                                                      .asTerm()
                                                                      .build();
}
