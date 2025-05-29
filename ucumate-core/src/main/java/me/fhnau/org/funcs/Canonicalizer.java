package me.fhnau.org.funcs;

import me.fhnau.org.UCUMDefinition.*;
import me.fhnau.org.UCUMRegistry;
import me.fhnau.org.builders.CombineTermBuilder;
import me.fhnau.org.builders.SoloTermBuilder;
import me.fhnau.org.funcs.Validator.Failure;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.model.UCUMExpression.*;
import me.fhnau.org.model.special.SpecialUnits;
import me.fhnau.org.model.special.SpecialUnitsFunctionProvider;
import me.fhnau.org.util.PreciseDecimal;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class Canonicalizer {

    private static final UCUMRegistry registry = UCUMRegistry.getInstance();

    public static final Cache<UCUMExpression, CanonicalStepResult> cache = Caffeine.newBuilder().maximumSize(10_000).recordStats().build();

    public record CanonicalStepResult(
        Term term,
        PreciseDecimal magnitude,
        PreciseDecimal cfPrefix,
        boolean specialHandlingActive,
        UCUMFunction specialFunction
    ) {

        CanonicalStepResult withTerm(Term newTerm) {
            return new CanonicalStepResult(newTerm, magnitude, cfPrefix, specialHandlingActive, specialFunction);
        }
    }

    private CanonicalStepResult multiplyValues(CanonicalStepResult left, CanonicalStepResult right) {
        if(left.specialHandlingActive && right.specialHandlingActive) {
            throw new RuntimeException("Dont know what to do yet.");
        }
        else if(left.specialHandlingActive) {
            return new CanonicalStepResult(
                null,
                left.magnitude,
                left.cfPrefix.multiply(right.magnitude),
                true,
                left.specialFunction
            );
        }
        else if(right.specialHandlingActive) {
            return new CanonicalStepResult(
                null,
                right.magnitude,
                right.cfPrefix.multiply(left.magnitude),
                true,
                right.specialFunction
            );
        }
        else {
            return new CanonicalStepResult(
                null,
                left.magnitude.multiply(right.magnitude),
                PreciseDecimal.ONE,
                false,
                null
            );
        }
        /*

        if (left.specialHandlingActive || right.specialHandlingActive) {
            // If either side has special handling, multiply cfPrefix only
            return new CanonicalStepResult(
                null,
                left.cfPrefix.multiply(right.cfPrefix),
                left.magnitude.multiply(right.magnitude),
                true,
                left.specialFunction != null ? left.specialFunction : right.specialFunction
            );
        } else {
            return new CanonicalStepResult(
                null,
                left.magnitude.multiply(right.magnitude),
                PreciseDecimal.ONE,
                false,
                null
            );
        }

         */
    }

    private CanonicalStepResult divideValues(CanonicalStepResult left, CanonicalStepResult right) {
        if (left.specialHandlingActive || right.specialHandlingActive) {
            return new CanonicalStepResult(
                null,
                left.magnitude,
                left.cfPrefix.divide(right.magnitude).divide(right.cfPrefix),
                true,
                left.specialFunction != null ? left.specialFunction : right.specialFunction
            );
        } else {
            return new CanonicalStepResult(
                null,
                left.magnitude.divide(right.magnitude),
                PreciseDecimal.ONE,
                false,
                null
            );
        }
    }

    private CanonicalStepResult powValue(CanonicalStepResult input, int exponent) {
        return input.specialHandlingActive()
            ? new CanonicalStepResult(
            input.term(),
            input.magnitude(),
            input.cfPrefix().pow(exponent),
            true,
            input.specialFunction()
        )
            : new CanonicalStepResult(
                input.term(),
                input.magnitude().pow(exponent),
                PreciseDecimal.ONE,
                false,
                null
            );
    }


    /*
    private class CanonicalStepIgnore {
        private static enum Area {
            MAIN_LEVEL, NESTED_LEVEL
        }

        private PreciseDecimal cFPrefix;
        private PreciseDecimal magnitude;
        /*
        By default, every conversion factor is combined in magnitude.
        As soon as a special unit is encountered, the current magnitude is written into cFPrefix,
        the "def" of the special unit is written into magnitude, and any subsequent conversion factors
        write into cFPrefix instead.
        This structure should work because if a special unit is present that means that all other components
        in this term can only be integer units or prefixes for said special unit. And they can only be multiplied, no division.

        Examples:
        "5.cm"
        1) 5 is written into magnitude
        2) c=0.01 is written into magnitude. magnitude=5*0.01=0.05
        3) No special unit encountered, magnitude=0.05 and cFPrefix=1

        "5.k[degF]"
        1) 5 is written into magnitude
        2) k=1000 is written into magnitude. magnitude=5*1000=5000
        3) [degF] encountered, move magnitude. magnitude=1, cFPrefix=5000
        4) Definition from [degF] into magnitude. magnitude=5/9, cFPrefix=5000

        "5.k[degF].7.2"
        1) 5 is written into magnitude
        2) k=1000 is written into magnitude. magnitude=5*1000=5000
        3) [degF] encountered, move magnitude. magnitude=1, cFPrefix=5000
        4) Definition from [degF] into magnitude. magnitude=5/9, cFPrefix=5000
        5) 7 is written into cFPrefix. magnitude=5/9, cFPrefix=7*5000=35000
        6) 2 is written into cFPrefix. magnitude=5/9 cFPrefix=2*35000=70000

        private boolean writeToMagnitude = true;
        private SpecialUnitsFunctionProvider.ConversionFunction specialFunction;
        private Term term;

        private List<PreciseDecimal> cfPrefixFactors = new ArrayList<>();
        private List<PreciseDecimal> magnitudeFactors = new ArrayList<>();

        public CanonicalStep(PreciseDecimal cFPrefix, PreciseDecimal magnitude, Term term) {
            this.cFPrefix = cFPrefix;
            this.magnitude = magnitude;
            this.term = term;
        }

        public CanonicalStep multiplyValue(PreciseDecimal preciseDecimal) {
            if(writeToMagnitude) {
                magnitudeFactors.add(magnitude);
                magnitude = preciseDecimal.multiply(magnitude);
            }
            else {
                cfPrefixFactors.add(cFPrefix);
                cFPrefix = preciseDecimal.multiply(cFPrefix);
            }
            return this;
        }

        public CanonicalStep divideValue(PreciseDecimal preciseDecimal) {
            if(writeToMagnitude) {
                magnitude = magnitude.divide(preciseDecimal);
            }
            else {
                cFPrefix = cFPrefix.divide(preciseDecimal);
            }
            return this;
        }

        public CanonicalStep powValue(int exponent) {
            if(writeToMagnitude) {
                magnitude = magnitude.pow(exponent);
            }
            else {
               cFPrefix = cFPrefix.pow(exponent);
            }
            return this;
        }

        public PreciseDecimal getValue() {
            if(writeToMagnitude) {
                return magnitude;
            }
            else {
                return cFPrefix;
            }
        }

        public Term getTerm() {
            return term;
        }

        public CanonicalStep setTerm(Term term) {
            this.term = term;
            return this;
        }

        public CanonicalStep setWriteToMagnitudeAndSwap(boolean writeToMagnitude) {
            if(!writeToMagnitude) {
                PreciseDecimal tmp = this.magnitude;
                this.magnitude = this.cFPrefix;
                this.cFPrefix = tmp;
            }
            this.writeToMagnitude = writeToMagnitude;
            return this;
        }

        public CanonicalStep setSpecialFunction(SpecialUnitsFunctionProvider.ConversionFunction specialFunction) {
            this.specialFunction = specialFunction;
            return this;
        }
    }

    */

    public enum UnitDirection {
        FROM, TO
    }

    public CanonicalizationResult canonicalize(Term term) {
        return canonicalize(PreciseDecimal.ONE, term);
    }

    public CanonicalizationResult canonicalize(PreciseDecimal factor, Term term) {
        return canonicalize(factor, term, true, true, UnitDirection.FROM);
    }

    public CanonicalizationResult canonicalize(PreciseDecimal factor, Term term, boolean normalize, boolean flatten, UnitDirection unitDirection) {
        try {
            CanonicalStepResult canonicalStep = canonicalizeImpl(term, new CanonicalStepResult(term, PreciseDecimal.ONE, PreciseDecimal.ONE, false, null));
            if(!(canonicalStep.term() instanceof CanonicalTerm canonicalTerm)) {
                throw new RuntimeException("Expected CanonicalTerm, got " + canonicalStep.term());
            }
            CanonicalTerm resultTerm = canonicalTerm;
            if(normalize) {
                resultTerm = (CanonicalTerm) new Normalizer().normalize(canonicalTerm);
            }
            if(flatten) {
                resultTerm = Flattener.flattenAndCancel(resultTerm);
            }
            boolean isSpecial = canonicalStep.specialHandlingActive() && canonicalStep.specialFunction() != null;


            PreciseDecimal resultFactor = switch (unitDirection) {
                case FROM -> {
                    if (isSpecial) {

                        SpecialUnitsFunctionProvider.ConversionFunction specialConvFunc = SpecialUnits.getFunction(canonicalStep.specialFunction().name());
                        //PreciseDecimal specialFactor = extractPrefixOrDimlessFactorFromSpecialUnit(canonicalStep.specialFunction().unit());
                        //PreciseDecimal Q_0 = canonicalStep.specialFunction().value().multiply(specialFactor).multiply(canonicalStep.cfPrefix());
                        PreciseDecimal factorAsInputForSpecialFunc = factor.multiply(canonicalStep.cfPrefix());
                        PreciseDecimal scaledRatio = specialConvFunc.toCanonical(factorAsInputForSpecialFunc);
                        yield canonicalStep.magnitude().multiply(scaledRatio);


                        //yield specialConvFunc.inverse(factor).multiply(Q_0.multiply(canonicalStep.cfPrefix()));
                        //yield specialConvFunc.inverse(factor.multiply(canonicalStep.specialFunction().value()).multiply(canonicalStep.cfPrefix().multiply(specialFactor)).multiply(canonicalStep.magnitude()));
                            //.multiply(canonicalStep.magnitude());
                    } else {
                        yield factor.multiply(canonicalStep.magnitude());
                    }
                }
                case TO -> {
                    if (isSpecial) {
                        SpecialUnitsFunctionProvider.ConversionFunction specialConvFunc = SpecialUnits.getFunction(canonicalStep.specialFunction().name());
                        //PreciseDecimal factorAsInputForSpecialFunc = factor.multiply(canonicalStep.cfPrefix());
                        PreciseDecimal factorAsInputForSpecialFunc = factor.divide(canonicalStep.magnitude());
                        PreciseDecimal scaledRatio = specialConvFunc.fromCanonical(factorAsInputForSpecialFunc);
                        scaledRatio = scaledRatio.multiply(PreciseDecimal.ONE.divide(canonicalStep.cfPrefix()));
                        yield scaledRatio;

                        //PreciseDecimal specialFactor = extractPrefixOrDimlessFactorFromSpecialUnit(canonicalStep.specialFunction().unit());
                        //yield specialConvFunc.convert(factor.divide(canonicalStep.magnitude())).divide(specialFactor).divide(canonicalStep.cfPrefix());
                        //yield specialConvFunc.convert(factor.multiply(canonicalStep.specialFunction().value()).divide(canonicalStep.cfPrefix()).divide(specialFactor).divide(canonicalStep.magnitude()));
                            //.divide(canonicalStep.magnitude());
                    } else {
                        yield factor.divide(canonicalStep.magnitude());
                    }
                }
            };
            return new Success(resultFactor, resultTerm);
        } catch (TermHasArbitraryUnitException e) {
            return new TermHasArbitraryUnit(e.arbitraryUnit);
        }
    }

    private PreciseDecimal extractPrefixOrDimlessFactorFromSpecialUnit(String specialUnitFunctionUnit) {
        return switch (Validator.validate(specialUnitFunctionUnit)) {
            case Failure failure -> throw new RuntimeException("Failed to extract prefix or dimless factor from special unit definition " + specialUnitFunctionUnit);
            case Validator.Success success -> extractPrefixOrDimlessFactorFromSpecialUnitImpl(success.term(), PreciseDecimal.ONE);
        };
    }

    private PreciseDecimal extractPrefixOrDimlessFactorFromSpecialUnitImpl(Term term, PreciseDecimal factor) {
        return switch (term) {
            case ComponentTerm componentTerm -> switch (componentTerm.component()) {
                case ComponentNoExponent componentNoExponent -> extractPrefixOrDimlessFactorFromComponent(componentNoExponent, factor);
                case ComponentExponent componentExponent -> extractPrefixOrDimlessFactorFromComponent(componentExponent, factor).pow(componentExponent.exponent().exponent());
            };
            case BinaryTerm binaryTerm -> {
                PreciseDecimal leftFactor = extractPrefixOrDimlessFactorFromSpecialUnitImpl(binaryTerm.left(), factor);
                PreciseDecimal rightFactor = extractPrefixOrDimlessFactorFromSpecialUnitImpl(binaryTerm.right(), factor);
                yield factor.multiply(leftFactor.multiply(rightFactor));
            }
            case UnaryDivTerm unaryDivTerm -> extractPrefixOrDimlessFactorFromSpecialUnitImpl(unaryDivTerm.term(), PreciseDecimal.ONE.divide(factor));
            case ParenTerm parenTerm -> extractPrefixOrDimlessFactorFromSpecialUnitImpl(parenTerm.term(), factor);
            case AnnotTerm annotTerm -> extractPrefixOrDimlessFactorFromSpecialUnitImpl(annotTerm.term(), factor);
            case AnnotOnlyTerm annotOnlyTerm -> PreciseDecimal.ONE;
        };
    }

    private PreciseDecimal extractPrefixOrDimlessFactorFromComponent(Component component, PreciseDecimal factor) {
        return switch (component.unit()) {
            case PrefixSimpleUnit prefixSimpleUnit -> factor.multiply(prefixSimpleUnit.prefix().value()
                .conversionFactor()).multiply(extractPrefixOrDimlessFactorFromUCUMUnitImpl(prefixSimpleUnit.ucumUnit()));
            case NoPrefixSimpleUnit noPrefixSimpleUnit -> extractPrefixOrDimlessFactorFromUCUMUnitImpl(noPrefixSimpleUnit.ucumUnit());
            case IntegerUnit integerUnit -> integerUnit.asPreciseDecimal();
        };
    }

    private PreciseDecimal extractPrefixOrDimlessFactorFromUCUMUnitImpl(UCUMUnit ucumUnit) {
        return switch (ucumUnit) {
            case BaseUnit baseUnit -> PreciseDecimal.ONE;
            case DimlessUnit dimlessUnit -> dimlessUnit.value().conversionFactor();
            case DefinedUnit definedUnit -> PreciseDecimal.ONE;
        };
    }

    private CanonicalStepResult canonicalizeImpl(Term term, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult cached = cache.getIfPresent(term);
        if(cached != null) {
            return cached;
        }
        CanonicalStepResult result = switch (term) {
            case ComponentTerm componentTerm -> handleCompTerm(componentTerm, canonicalStep);
            case BinaryTerm binaryTerm -> handleBinaryTerm(binaryTerm, canonicalStep);
            case UnaryDivTerm unaryDivTerm -> canonicalizeImpl(
                CombineTermBuilder.builder().left(SoloTermBuilder.UNITY).divideBy().right(unaryDivTerm.term()).build(), canonicalStep);
            case ParenTerm parenTerm -> canonicalizeImpl(parenTerm.term(), canonicalStep);
            case AnnotTerm annotTerm -> canonicalizeImpl(annotTerm.term(), canonicalStep);
            case AnnotOnlyTerm annotOnlyTerm -> new CanonicalStepResult(SoloTermBuilder.UNITY, PreciseDecimal.ONE, PreciseDecimal.ONE, canonicalStep.specialHandlingActive(), canonicalStep.specialFunction());
        };
        cache.put(term, result);
        return result;
    }

    private CanonicalStepResult handleBinaryTerm(BinaryTerm binaryTerm, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult leftStep = canonicalizeImpl(binaryTerm.left(), canonicalStep);
        CanonicalStepResult rightStep = canonicalizeImpl(binaryTerm.right(), canonicalStep);
        CanonicalStepResult combineValue = switch (binaryTerm.operator()) {
            case MUL -> multiplyValues(leftStep, rightStep);
            case DIV -> divideValues(leftStep, rightStep);
        };

        Term leftTerm = leftStep.term();
        Term rightTerm = rightStep.term();

        Term resultTerm = (leftTerm instanceof CanonicalTerm l && rightTerm instanceof CanonicalTerm r)
            ? new CanonicalBinaryTerm(l, binaryTerm.operator(), r)
            : new MixedBinaryTerm(leftTerm, binaryTerm.operator(), rightTerm);
        return combineValue.withTerm(resultTerm);
    }

    private CanonicalStepResult handleCompTerm(ComponentTerm componentTerm, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult unitStep = canonicalizeUnit(componentTerm.component().unit(), canonicalStep);
        return switch (componentTerm.component()) {
            case ComponentNoExponent componentNoExponent -> unitStep;
            case ComponentExponent componentExponent -> {
                CanonicalStepResult raised = powValue(unitStep, componentExponent.exponent().exponent());
                Term remainingTermWithoutPrefix = removePrefixes(unitStep.term(), componentExponent.exponent().exponent());
                yield raised.withTerm(remainingTermWithoutPrefix);
            }
        };
    }

    private CanonicalStepResult canonicalizeUnit(Unit unit, CanonicalStepResult canonicalStepResult)
        throws TermHasArbitraryUnitException {
        return switch (unit) {
            case IntegerUnit integerUnit -> {
                //yield new CanonicalStepResult(SoloTermBuilder.UNITY, integerUnit.asPreciseDecimal(), PreciseDecimal.ONE, false, null);

                if(canonicalStepResult.specialHandlingActive()) {
                    // We are inside the canonicalization part of a special unit, write to cfPrefix
                    yield new CanonicalStepResult(SoloTermBuilder.UNITY, PreciseDecimal.ONE, integerUnit.asPreciseDecimal(), true, canonicalStepResult.specialFunction());

                }
                else {
                    // We are either on the same level as a special unit (i.e. "5.Cel") or there is no special unit involved at all
                    yield new CanonicalStepResult(SoloTermBuilder.UNITY, integerUnit.asPreciseDecimal(), canonicalStepResult.cfPrefix(), false, null);
                    // Write to magnitude
                }
            }
            //case IntegerUnit integerUnit -> new CanonicalStepResult(SoloTermBuilder.UNITY, integerUnit.asPreciseDecimal(), PreciseDecimal.ONE, false, null);
            case NoPrefixSimpleUnit noPrefixSimpleUnit -> canonicalizeUCUMConcept(noPrefixSimpleUnit.ucumUnit(), canonicalStepResult);
            case PrefixSimpleUnit prefixSimpleUnit -> canonicalizePrefixSimpleUnit(prefixSimpleUnit, canonicalStepResult);
        };
    }

    private CanonicalStepResult canonicalizePrefixSimpleUnit(PrefixSimpleUnit prefixSimpleUnit, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {

        PreciseDecimal factor = prefixSimpleUnit.prefix().value().conversionFactor();
        Term unitOnly = SoloTermBuilder.builder().withoutPrefix(prefixSimpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
        /*
        return switch (prefixSimpleUnit.ucumUnit()) {
            case BaseUnit baseUnit -> canonicalizeImpl(unitOnly, canonicalStep);
            case DerivedUnit definedUnit -> canonicalizeImpl(unitOnly, canonicalStep);
            case ArbitraryUnit arbitraryUnit -> canonicalizeImpl(unitOnly, canonicalStep);
            case DimlessUnit dimlessUnit -> canonicalizeImpl(unitOnly, canonicalStep);
            case SpecialUnit specialUnit -> canonicalizeImpl(unitOnly, canonicalStep);
            //case SpecialUnit specialUnit -> new CanonicalStepResult(canonicalizeImpl(unitOnly, canonicalStep).term(), canonicalStep.magnitude(), canonicalStep.cfPrefix().multiply(factor), false, null);
        };

         */
        CanonicalStepResult unitOnlyStep = canonicalizeImpl(unitOnly, canonicalStep); // ? null
        return composeConsideringSpecial(unitOnlyStep, factor);
        //return canonicalizeUCUMConcept(prefixSimpleUnit.prefix(), canonicalStep);
    }

    private CanonicalStepResult canonicalizeUCUMConcept(Concept concept, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {
        return switch (concept) {
            case UCUMPrefix ucumPrefix -> composeConsideringSpecial(canonicalStep, ucumPrefix.value().conversionFactor()); //new CanonicalStepResult(SoloTermBuilder.UNITY, ucumPrefix.value().conversionFactor(), PreciseDecimal.ONE, false, null);
            case BaseUnit baseUnit -> new CanonicalStepResult(SoloTermBuilder.builder().withoutPrefix(baseUnit).noExpNoAnnot().asTerm().build(), PreciseDecimal.ONE, PreciseDecimal.ONE, false, null);
            case DerivedUnit derivedUnit -> canonicalizeDerivedOrDimlessUnit(derivedUnit, canonicalStep);
            case DimlessUnit dimlessUnit -> canonicalizeDerivedOrDimlessUnit(dimlessUnit, canonicalStep);
            case SpecialUnit specialUnit -> canonicalizeSpecialUnit(specialUnit, canonicalStep);
            case ArbitraryUnit arbitraryUnit -> throw new TermHasArbitraryUnitException(arbitraryUnit);
        };

    }

    private CanonicalStepResult canonicalizeSpecialUnit(SpecialUnit specialUnit, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {
        Term sourceDef = registry.getDefinedUnitSourceDefinition(specialUnit);
        CanonicalStepResult inner = canonicalizeImpl(sourceDef, new CanonicalStepResult(canonicalStep.term(), canonicalStep.magnitude(), canonicalStep.cfPrefix(), false, null));
        return new CanonicalStepResult(
            inner.term(),
            inner.magnitude().multiply(specialUnit.value().function().value()),
            canonicalStep.magnitude(),
            true,
            specialUnit.value().function()
        );
    }

    private CanonicalStepResult canonicalizeDerivedOrDimlessUnit(DefinedUnit definedUnit, CanonicalStepResult canonicalStep)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult inner = canonicalizeImpl(registry.getDefinedUnitSourceDefinition(definedUnit), canonicalStep);
        return composeConsideringSpecial(
            inner,
            definedUnit.value().conversionFactor()
        );
        //return inner.withTerm(inner.term());
    }

    private Term removePrefixes(Term term, int exponent) {
        return switch (term) {
            case BinaryTerm binaryTerm -> {
                var tmp = switch (binaryTerm.operator()) {
                    case MUL -> CombineTermBuilder.builder().left(removePrefixes(binaryTerm.left(), exponent)).multiplyWith().right(removePrefixes(binaryTerm.right(), exponent)).build();
                    case DIV -> CombineTermBuilder.builder().left(removePrefixes(binaryTerm.left(), exponent)).divideBy().right(removePrefixes(binaryTerm.right(), exponent)).build();
                };
                yield tmp;
            }
            case UnaryDivTerm unaryDivTerm -> CombineTermBuilder.builder().unaryDiv().right(removePrefixes(unaryDivTerm.term(), exponent)).build();
            case ParenTerm parenTerm -> removePrefixes(parenTerm.term(), exponent);
            case AnnotTerm annotTerm -> removePrefixes(annotTerm.term(), exponent);
            case AnnotOnlyTerm annotOnlyTerm -> SoloTermBuilder.UNITY;
            case ComponentTerm componentTerm -> {
                int existingExponent = switch (componentTerm.component()) {
                    case ComponentExponent componentExponent -> exponent * componentExponent.exponent().exponent(); // not to sure about the multiplication here being correct :/
                    case ComponentNoExponent componentNoExponent -> exponent;
                };
                Unit unit = componentTerm.component().unit();
                var tmp = switch (unit) {
                    case SimpleUnit simpleUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).asComponent().withExponent(existingExponent).withoutAnnotation().asTerm().build(); // todo do I need the exponent here?
                    case IntegerUnit integerUnit -> SoloTermBuilder.builder().withIntegerUnit(integerUnit.value()).asComponent().withExponent(existingExponent).withoutAnnotation().asTerm().build();
                };
                //System.out.println(new WolframAlphaSyntaxPrinter().print(tmp));
                yield tmp;
            }
        };
    }

    private CanonicalStepResult composeConsideringSpecial(CanonicalStepResult canonicalStepResult, PreciseDecimal factor) {
        if(canonicalStepResult.specialHandlingActive()) {
            return new CanonicalStepResult(
                canonicalStepResult.term(),
                canonicalStepResult.magnitude(),
                canonicalStepResult.cfPrefix().multiply(factor),
                true,
                canonicalStepResult.specialFunction());
        }
        else {
            return new CanonicalStepResult(
                canonicalStepResult.term(),
                canonicalStepResult.magnitude().multiply(factor),
                canonicalStepResult.cfPrefix(),
                false,
                null
            );
        }
    }

    private static class TermHasArbitraryUnitException extends Throwable {
        private final ArbitraryUnit arbitraryUnit;

        public TermHasArbitraryUnitException(ArbitraryUnit arbitraryUnit) {
            this.arbitraryUnit = arbitraryUnit;
        }
    }

    public sealed interface CanonicalizationResult {}

    public sealed interface FailedCanonicalization extends CanonicalizationResult {}

    public record Success(PreciseDecimal magnitude, CanonicalTerm canonicalTerm) implements
        CanonicalizationResult {}

    public record TermHasArbitraryUnit(ArbitraryUnit arbitraryUnit) implements
        FailedCanonicalization {}

}
