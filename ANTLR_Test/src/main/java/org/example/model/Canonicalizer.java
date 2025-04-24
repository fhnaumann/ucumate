package org.example.model;

import org.example.UCUMDefinition;
import org.example.UCUMRegistry;
import org.example.builders.CombineTermBuilder;
import org.example.builders.SoloTermBuilder;
import org.example.funcs.Normalizer;
import org.example.util.PreciseDecimal;

import java.util.Optional;

public class Canonicalizer {

    private static final UCUMRegistry registry = UCUMRegistry.getInstance();

    public record CanonicalStep(PreciseDecimal conversionFactor, Expression.Term term) {}


    public CanonicalizationResult canonicalize(Expression.Term term) {
        SpecialUnitConversionContext specialUnitConversionContext;
        return null;
    }

    public CanonicalizationResult canonicalize(Expression.Term term, SpecialUnitConversionContext specialContext) {
        try {
            CanonicalStep canonicalStep = canonicalizeImpl(term, specialContext);
            if(!(canonicalStep.term() instanceof Expression.CanonicalTerm canonicalTerm)) {
                throw new RuntimeException("Expected CanonicalTerm, got " + canonicalStep.term);
            }
            Expression.CanonicalTerm normalizedTerm = (Expression.CanonicalTerm) new Normalizer().normalize(canonicalTerm);
            return new Success(canonicalStep.conversionFactor(), normalizedTerm, specialContext.direction());
        } catch(TermHasArbitraryUnitException e) {
            return new TermHasArbitraryUnit(e.arbitraryUnit);
        }
    }

    private CanonicalStep canonicalizeImpl(Expression.Term term, SpecialUnitConversionContext specialContext) throws TermHasArbitraryUnitException {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> handleCompTerm(componentTerm, specialContext);
            case Expression.BinaryTerm binaryTerm -> handleBinaryTerm(binaryTerm, specialContext);
            case Expression.UnaryDivTerm unaryDivTerm -> {
                CanonicalStep result = canonicalizeImpl(unaryDivTerm.term(), specialContext);
                // The term should be kept in the denominator
                // todo: should this be a binaryterm(1/term) or unarydivterm(term)?
//                Expression.Term canonicalStepTermInDenominator = CombineTermBuilder.builder()
//                                                            .unaryDiv()
//                                                            .right(result.term())
//                                                            .build();
                Expression.Term canonicalStepTermInDenominator = CombineTermBuilder.builder()
                        .left(SoloTermBuilder.builder().withIntegerUnit(1).noExpNoAnnot().asTerm().build())
                        .divideBy()
                        .right(result.term())
                        .build();

                yield new CanonicalStep(PreciseDecimal.ONE.divide(result.conversionFactor()), canonicalStepTermInDenominator); // todo 1d /... probably wont work for special units, see how binary units handle it
            }
            case Expression.AnnotOnlyTerm _ -> new CanonicalStep(PreciseDecimal.ONE, term);
            case Expression.AnnotTerm annotTerm -> canonicalizeImpl(annotTerm.term(), specialContext);
            case Expression.ParenTerm parenTerm -> canonicalizeImpl(parenTerm.term(), specialContext);
        };
    }

    private CanonicalStep handleCompTerm(Expression.ComponentTerm componentTerm, SpecialUnitConversionContext specialContext) throws TermHasArbitraryUnitException {
        CanonicalStep canonicalStep = canonicalizeUnit(componentTerm.component().unit(), specialContext);
        return switch(componentTerm.component()) {
            case Expression.ComponentExponent componentExponent -> {
                PreciseDecimal conversionFactor = canonicalStep.conversionFactor.pow(componentExponent.exponent().exponent());
                // Since UCUM 1.9 only units (with prefixes) can have exponents. Arbitrary (nested) terms (defined through brackets)
                // may not have an exponent. Therefore, it's safe to assume the term in the canonical step is a CompTerm
                // with a unit without an exponent.
                // todo might be broken for terms without any units at all because the casting to SimpleUnit would fail
                Expression.SimpleUnit canonicalStepUnit = (Expression.SimpleUnit) ((Expression.ComponentTerm) canonicalStep.term()).component().unit();
                // We just calculated the exponent into the conversion factor, but unlike prefixes, the exponent should stay
                // during the canonicalization process.
                Expression.Term term = SoloTermBuilder.builder()
                                                      .withoutPrefix(canonicalStepUnit.ucumUnit()) // prefix was removed by the canonicalizeUnit call at the start of this method
                                                      .asComponent()
                                                      .withExponent(componentExponent.exponent().exponent())
                                                      .withoutAnnotation() // don't care about annotations in the canonical representation of the term
                                                      .asTerm()
                                                      .build();

                yield new CanonicalStep(conversionFactor, term);
            }
            case Expression.ComponentNoExponent _ -> canonicalStep;
        };
    }

    private CanonicalStep handleBinaryTerm(Expression.BinaryTerm binaryTerm, SpecialUnitConversionContext specialContext) throws TermHasArbitraryUnitException {
        /*
        There may be a special case:
        This method is called where either term is a factor and the other is a special unit.
        In this case, we need to incorporate the factor into the special unit handling function that is reached when
        'canonicalizeImpl' is called with the special unit (as a component term).
        In all other scenarios, it is fine to canonicalize both terms first and then multiply/divide their conversion factors.
         */
        SpecialUnitCanonicalStepContainer specialUnitCanonicalStepContainer = handlePotentialSpecialUnitInConversionFactor(binaryTerm, specialContext);
        CanonicalStep leftResult;
        CanonicalStep rightResult;
        if(specialUnitCanonicalStepContainer != null) {
            leftResult = specialUnitCanonicalStepContainer.leftStep();
            rightResult = specialUnitCanonicalStepContainer.rightStep();
        }
        else {
            leftResult = canonicalizeImpl(binaryTerm.left(), specialContext);
            rightResult = canonicalizeImpl(binaryTerm.right(), specialContext);
        }
        PreciseDecimal mulConversionFactor = leftResult.conversionFactor().multiply(rightResult.conversionFactor());
        PreciseDecimal divConversionFactor = leftResult.conversionFactor().divide(rightResult.conversionFactor());
        if(leftResult.term() instanceof Expression.CanonicalTerm leftCanonicalTerm && rightResult.term() instanceof Expression.CanonicalTerm rightCanonicalTerm) {
                return switch(binaryTerm.operator()) {
                    case MUL -> new CanonicalStep(mulConversionFactor,
                                                  new Expression.CanonicalBinaryTerm(leftCanonicalTerm,
                                                                                     Expression.Operator.MUL,
                                                                                     rightCanonicalTerm
                                                  )
                    );
                    case DIV -> new CanonicalStep(divConversionFactor,
                                                  new Expression.CanonicalBinaryTerm(leftCanonicalTerm,
                                                                                     Expression.Operator.DIV,
                                                                                     rightCanonicalTerm
                                                  )
                    );
                };
        }
        else {
            return switch(binaryTerm.operator()) {
                case MUL -> new CanonicalStep(mulConversionFactor,
                                              new Expression.MixedBinaryTerm(leftResult.term(),
                                                                             Expression.Operator.MUL,
                                                                             rightResult.term()
                                              )
                );
                case DIV -> new CanonicalStep(divConversionFactor,
                                              new Expression.MixedBinaryTerm(leftResult.term(),
                                                                             Expression.Operator.DIV,
                                                                             rightResult.term()
                                              )
                );
            };
        }
    }

    private record SpecialUnitCanonicalStepContainer(CanonicalStep leftStep, CanonicalStep rightStep) {}

    private SpecialUnitCanonicalStepContainer handlePotentialSpecialUnitInConversionFactor(Expression.BinaryTerm binaryTerm, SpecialUnitConversionContext specialContext) throws TermHasArbitraryUnitException {
        boolean leftIsSpecial = isSpecialUnitAsComponentTerm(binaryTerm.left());
        Optional<Integer> leftIsIntegerOpt = isIntegerUnit(binaryTerm.left());
        boolean rightIsSpecial = isSpecialUnitAsComponentTerm(binaryTerm.right());
        Optional<Integer> rightIsIntegerOpt = isIntegerUnit(binaryTerm.right());
        if(leftIsIntegerOpt.isPresent() && rightIsSpecial) {
            CanonicalStep leftResult = canonicalizeImpl(binaryTerm.left(), specialContext);
            CanonicalStep rightResult = canonicalizeImpl(binaryTerm.right(), new SpecialUnitConversionContext(specialContext.factor().multiply(leftResult.conversionFactor()), specialContext.direction()));
            leftResult = new CanonicalStep(PreciseDecimal.ONE, leftResult.term()); // erase left convFactor because it is accounted for already in the right CanonicalStep
            return new SpecialUnitCanonicalStepContainer(leftResult, rightResult);
        }
        else if(rightIsIntegerOpt.isPresent() && leftIsSpecial) {
            CanonicalStep rightResult = canonicalizeImpl(binaryTerm.right(), specialContext);
            CanonicalStep leftResult = canonicalizeImpl(binaryTerm.left(), new SpecialUnitConversionContext(specialContext.factor().multiply(rightResult.conversionFactor()), specialContext.direction()));
            rightResult = new CanonicalStep(PreciseDecimal.ONE, rightResult.term()); // erase right convFactor because it is accounted for already in the left CanonicalStep
            return new SpecialUnitCanonicalStepContainer(leftResult, rightResult);
        }
        else {
            // no special unit involved
            return null;
        }
    }

    private Optional<Integer> isIntegerUnit(Expression.Term term) {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> switch(componentTerm.component().unit()) {
                case Expression.SimpleUnit _ -> Optional.empty();
                case Expression.IntegerUnit integerUnit -> Optional.of(integerUnit.value());
            };
            case Expression.ParenTerm parenTerm -> isIntegerUnit(parenTerm.term());
            case Expression.AnnotTerm annotTerm -> isIntegerUnit(annotTerm.term());
            case Expression.AnnotOnlyTerm _, Expression.BinaryTerm _, Expression.UnaryDivTerm _ -> Optional.empty();
        };
    }

    private boolean isSpecialUnitAsComponentTerm(Expression.Term term) {
        return switch(term) {
            case Expression.ComponentTerm componentTerm -> switch(componentTerm.component().unit()) {
                case Expression.SimpleUnit simpleUnit -> switch(simpleUnit.ucumUnit()) {
                    case UCUMDefinition.SpecialUnit _ -> true;
                    case UCUMDefinition.BaseUnit _, UCUMDefinition.DefinedUnit _ -> false;
                };
                case Expression.IntegerUnit _ -> false;
            };
            case Expression.ParenTerm parenTerm -> isSpecialUnitAsComponentTerm(parenTerm.term());
            case Expression.AnnotTerm annotTerm -> isSpecialUnitAsComponentTerm(annotTerm.term());
            case Expression.AnnotOnlyTerm _, Expression.BinaryTerm _, Expression.UnaryDivTerm _ -> false;
        };
    }

    private CanonicalStep canonicalizeUnit(Expression.Unit unit, SpecialUnitConversionContext specialContext) throws TermHasArbitraryUnitException {
        return switch(unit) {
            case Expression.IntegerUnit integerUnit -> new CanonicalStep(integerUnit.asPreciseDecimal(), SoloTermBuilder.UNITY);
            case Expression.PrefixSimpleUnit prefixSimpleUnit -> {
                CanonicalStep prefixCanonicalStep = canonicalizeUCUMConcept(prefixSimpleUnit.prefix(), specialContext);
                yield switch(prefixSimpleUnit.ucumUnit()) {
                    case UCUMDefinition.SpecialUnit _ -> {
                        specialContext = new SpecialUnitConversionContext(specialContext.factor().multiply(prefixCanonicalStep.conversionFactor()), specialContext.direction());
                        CanonicalStep canonicalStep = canonicalizeUCUMConcept(prefixSimpleUnit.ucumUnit(), specialContext);
                        yield new CanonicalStep(canonicalStep.conversionFactor(), canonicalStep.term());
                    }
                    case UCUMDefinition.BaseUnit _, UCUMDefinition.DefinedUnit _ -> {
                        CanonicalStep canonicalStep = canonicalizeUCUMConcept(prefixSimpleUnit.ucumUnit(), specialContext);
                        yield new CanonicalStep(prefixCanonicalStep.conversionFactor().multiply(canonicalStep.conversionFactor()),
                                                canonicalStep.term());
                    }
                };
            }
            case Expression.NoPrefixSimpleUnit noPrefixSimpleUnit ->
                    canonicalizeUCUMConcept(noPrefixSimpleUnit.ucumUnit(), specialContext);
        };
    }

    private CanonicalStep canonicalizeUCUMConcept(UCUMDefinition.Concept concept, SpecialUnitConversionContext specialContext) throws TermHasArbitraryUnitException {
        return switch(concept) {
            case UCUMDefinition.UCUMPrefix ucumPrefix -> new CanonicalStep(ucumPrefix.value().conversionFactor(), null);
            case UCUMDefinition.BaseUnit baseUnit -> new CanonicalStep(PreciseDecimal.ONE,
                                                                       SoloTermBuilder.builder().withoutPrefix(baseUnit).noExpNoAnnot().asTerm().build());
            /*
            new Expression.CompTerm(new Expression.ComponentNoExponent(
                                                                               new Expression.NoPrefixSimpleUnit(
                                                                                       baseUnit)))
            );*/
            case UCUMDefinition.DerivedUnit derivedUnit -> {
                CanonicalStep canonicalStep = canonicalizeImpl(registry.getDefinedUnitSourceDefinition(derivedUnit), specialContext);
                yield new CanonicalStep(canonicalStep.conversionFactor().multiply(derivedUnit.value().conversionFactor()),
                                        canonicalStep.term()
                );
            }
            case UCUMDefinition.DimlessUnit dimlessUnit -> {
                CanonicalStep canonicalStep = canonicalizeImpl(registry.getDefinedUnitSourceDefinition(dimlessUnit), specialContext);
                yield new CanonicalStep(canonicalStep.conversionFactor().multiply(dimlessUnit.value().conversionFactor()),
                                        canonicalStep.term()
                );
            }
            case UCUMDefinition.SpecialUnit specialUnit -> {
                // TODO below is probably at least partially wrong
                // the number into the conversion function is the scaling function of the entire term (1 for now)
                // Use .convert if from is special (i.e. Cel) and to is base (i.e. K)
                // Use .invert if from is base (i.e. K) and to is special (i.e. Cel)

                // We need two pieces of information here:
                // The scaling factor from the 'from' unit and
                // if the special unit conversion happens from special->base (.invert) or base->special (.convert)
                SpecialUnits.ConversionFunction conversionFunction = SpecialUnits.getFunction(specialUnit.value().function().name());
                double conversionFactor = switch(specialContext.direction()) {
                    case FROM -> {
                        yield conversionFunction.inverse(specialContext.factor().getValue().doubleValue()); // todo using double in special case for now
                    }
                    case TO -> {
                        yield conversionFunction.convert(specialContext.factor().getValue().doubleValue()); // todo using double in special case for now
                    }
                    case NO_SPECIAL_INVOLVED -> throw new RuntimeException("Special Unit found but check earlier said no special unit exists inside %s".formatted(concept));
                };
                // todo: I am using a fixed scale (4) for now. For more precision another (heavy) math library is needed. A feature flag?
                PreciseDecimal preciseDecimal = PreciseDecimal.fromDoubleFixedScale(conversionFactor);
                yield new CanonicalStep(preciseDecimal,
                                        registry.getDefinedUnitSourceDefinition(specialUnit)
                );
            }
            case UCUMDefinition.ArbitraryUnit arbitraryUnit -> throw new TermHasArbitraryUnitException(arbitraryUnit);
        };
    }

    public record SpecialUnitConversionContext(PreciseDecimal factor, SpecialUnitApplicationDirection direction) {}

    public enum SpecialUnitApplicationDirection {
        FROM, TO, NO_SPECIAL_INVOLVED
    }

    private static class TermHasArbitraryUnitException extends Throwable {
        private final UCUMDefinition.ArbitraryUnit arbitraryUnit;

        public TermHasArbitraryUnitException(UCUMDefinition.ArbitraryUnit arbitraryUnit) {
            this.arbitraryUnit = arbitraryUnit;
        }
    }

    public sealed interface CanonicalizationResult {}

    public sealed interface FailedCanonicalization extends CanonicalizationResult {}

    public record Success(PreciseDecimal conversionFactor, Expression.CanonicalTerm canonicalTerm, SpecialUnitApplicationDirection direction) implements CanonicalizationResult {}

    public record TermHasArbitraryUnit(UCUMDefinition.ArbitraryUnit arbitraryUnit) implements FailedCanonicalization {}
}
