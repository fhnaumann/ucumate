package io.github.fhnaumann.funcs;

import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.model.UCUMDefinition.*;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.MolMassUtil;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.builders.CombineTermBuilder;
import io.github.fhnaumann.builders.SoloTermBuilder;
import io.github.fhnaumann.funcs.Validator.Failure;
import io.github.fhnaumann.model.UCUMExpression.*;
import io.github.fhnaumann.model.special.SpecialUnits;
import io.github.fhnaumann.model.special.SpecialUnitsFunctionProvider;
import io.github.fhnaumann.util.PreciseDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Canonicalizer {

    private static final UCUMRegistry registry = UCUMRegistry.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Canonicalizer.class);

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

    public enum UnitDirection {
        FROM, TO
    }

    public CanonicalizationResult canonicalize(Term term) {
        return canonicalize(PreciseDecimal.ONE, term);
    }

    public CanonicalizationResult canonicalize(PreciseDecimal factor, Term term) {
        return canonicalize(factor, term, true, true, UnitDirection.FROM, PreciseDecimal.ONE);
    }

    public CanonicalizationResult canonicalize(PreciseDecimal factor, Term term, boolean normalize, boolean flatten, UnitDirection unitDirection, PreciseDecimal substanceMolarMassCoeff) {
        try {
            CanonicalStepResult canonicalStep = canonicalizeImpl(term, new CanonicalStepResult(term, PreciseDecimal.ONE, PreciseDecimal.ONE, false, null), substanceMolarMassCoeff);
            if(!(canonicalStep.term() instanceof CanonicalTerm canonicalTerm)) {
                throw new RuntimeException("Expected CanonicalTerm, got " + canonicalStep.term());
            }
            CanonicalTerm resultTerm = canonicalTerm;
            if(flatten) {
                resultTerm = Flattener.flattenAndCancel(resultTerm);
            }
            if(normalize) {
                resultTerm = (CanonicalTerm) new Normalizer().normalize(resultTerm);
            }
            // explicitly cache the result after normalizing and flatten
            //if(substanceMolarMassCoeff == null) {


            boolean isSpecial = canonicalStep.specialHandlingActive() && canonicalStep.specialFunction() != null;
            boolean isMolInvolved = MolMassUtil.containsMol(term);
            if(isSpecial && isMolInvolved && substanceMolarMassCoeff != null && ConfigurationRegistry.get().isEnableMolMassConversion()) {
                // as for UCUM version 2.2 this only affects "[pH]"
                log.warn("Conversion involving the special unit '[pH]' to a mass unit is not supported.");
                return new TermContainsPHAndCanonicalizingToMass();
            }

            /*
            Only save the canonical form if no mol is involved. When the key term contains the mole unit, then it depends on dynamic properties such as whether
            mol<->mass conversion is active and the provided substanceMolarMassCoefficient.
            In theory, it's possible to cache these, but that would require preserving these information in the key. Currently, only
            the term is being saved there, and it would require severe restructuring so it's easier to just skip caching in these instances.
             */
            if(!isMolInvolved) {
                PersistenceRegistry.getInstance().saveCanonical(term, new CanonicalStepResult(
                        resultTerm,
                        canonicalStep.magnitude,
                        canonicalStep.cfPrefix,
                        canonicalStep.specialHandlingActive,
                        canonicalStep.specialFunction
                ));
            }
            else if(log.isDebugEnabled() && PersistenceRegistry.hasAny()){
                log.debug("Not saving {} in cache because the mole unit requires additional properties to be stored in the key, which is not currently implemented.", UCUMService.print(term));
            }

            PreciseDecimal resultFactor = switch (unitDirection) {
                case FROM -> {
                    if (isSpecial) {
                        SpecialUnitsFunctionProvider.ConversionFunction specialConvFunc = SpecialUnits.getFunction(canonicalStep.specialFunction().name());
                        PreciseDecimal factorAsInputForSpecialFunc = factor.multiply(canonicalStep.cfPrefix());
                        PreciseDecimal scaledRatio = specialConvFunc.toCanonical(factorAsInputForSpecialFunc);
                        yield canonicalStep.magnitude().multiply(scaledRatio);
                    } else {
                        yield factor.multiply(canonicalStep.magnitude());
                    }
                }
                case TO -> {
                    if (isSpecial) {
                        SpecialUnitsFunctionProvider.ConversionFunction specialConvFunc = SpecialUnits.getFunction(canonicalStep.specialFunction().name());
                        PreciseDecimal factorAsInputForSpecialFunc = factor.divide(canonicalStep.magnitude());
                        PreciseDecimal scaledRatio = specialConvFunc.fromCanonical(factorAsInputForSpecialFunc);
                        scaledRatio = scaledRatio.multiply(PreciseDecimal.ONE.divide(canonicalStep.cfPrefix()));
                        yield scaledRatio;
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

    private CanonicalStepResult canonicalizeImpl(Term term, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult cached = PersistenceRegistry.getInstance().getCanonical(term);
        if(cached != null) {
            return cached;
        }
        CanonicalStepResult result = switch (term) {
            case ComponentTerm componentTerm -> handleCompTerm(componentTerm, canonicalStep, substanceMolarMassCoeff);
            case BinaryTerm binaryTerm -> handleBinaryTerm(binaryTerm, canonicalStep, substanceMolarMassCoeff);
            case UnaryDivTerm unaryDivTerm -> canonicalizeImpl(
                CombineTermBuilder.builder().left(SoloTermBuilder.UNITY).divideBy().right(unaryDivTerm.term()).build(), canonicalStep, substanceMolarMassCoeff);
            case ParenTerm parenTerm -> canonicalizeImpl(parenTerm.term(), canonicalStep, substanceMolarMassCoeff);
            case AnnotTerm annotTerm -> canonicalizeImpl(annotTerm.term(), canonicalStep, substanceMolarMassCoeff);
            case AnnotOnlyTerm annotOnlyTerm -> new CanonicalStepResult(SoloTermBuilder.UNITY, PreciseDecimal.ONE, PreciseDecimal.ONE, canonicalStep.specialHandlingActive(), canonicalStep.specialFunction());
        };
        //PersistenceRegistry.getInstance().saveCanonical(term, result);
        return result;
    }

    private CanonicalStepResult handleBinaryTerm(BinaryTerm binaryTerm, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult leftStep = canonicalizeImpl(binaryTerm.left(), canonicalStep, substanceMolarMassCoeff);
        CanonicalStepResult rightStep = canonicalizeImpl(binaryTerm.right(), canonicalStep, substanceMolarMassCoeff);
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

    private CanonicalStepResult handleCompTerm(ComponentTerm componentTerm, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult unitStep = canonicalizeUnit(componentTerm.component().unit(), canonicalStep, substanceMolarMassCoeff);
        return switch (componentTerm.component()) {
            case ComponentNoExponent componentNoExponent -> unitStep;
            case ComponentExponent componentExponent -> {
                CanonicalStepResult raised = powValue(unitStep, componentExponent.exponent().exponent());
                Term remainingTermWithoutPrefix = removePrefixes(unitStep.term(), componentExponent.exponent().exponent());
                yield raised.withTerm(remainingTermWithoutPrefix);
            }
        };
    }

    private CanonicalStepResult canonicalizeUnit(Unit unit, CanonicalStepResult canonicalStepResult, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        return switch (unit) {
            case IntegerUnit integerUnit -> {
                if(canonicalStepResult.specialHandlingActive()) {
                    // We are inside the canonicalization part of a special unit, write to cfPrefix
                    yield new CanonicalStepResult(SoloTermBuilder.UNITY, PreciseDecimal.ONE, integerUnit.asPreciseDecimal(), true, canonicalStepResult.specialFunction());

                }
                else {
                    // We are either on the same level as a special unit (i.e. "5.Cel") or there is no special unit involved at all
                    yield new CanonicalStepResult(SoloTermBuilder.UNITY, integerUnit.asPreciseDecimal(), canonicalStepResult.cfPrefix(), false, null);
                }
            }
            case NoPrefixSimpleUnit noPrefixSimpleUnit -> canonicalizeUCUMConcept(noPrefixSimpleUnit.ucumUnit(), canonicalStepResult, substanceMolarMassCoeff);
            case PrefixSimpleUnit prefixSimpleUnit -> canonicalizePrefixSimpleUnit(prefixSimpleUnit, canonicalStepResult, substanceMolarMassCoeff);
        };
    }

    private CanonicalStepResult canonicalizePrefixSimpleUnit(PrefixSimpleUnit prefixSimpleUnit, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {

        PreciseDecimal factor = prefixSimpleUnit.prefix().value().conversionFactor();
        Term unitOnly = SoloTermBuilder.builder().withoutPrefix(prefixSimpleUnit.ucumUnit()).noExpNoAnnot().asTerm().build();
        CanonicalStepResult unitOnlyStep = canonicalizeImpl(unitOnly, canonicalStep, substanceMolarMassCoeff); // ? null
        return composeConsideringSpecial(unitOnlyStep, factor);
        //return canonicalizeUCUMConcept(prefixSimpleUnit.prefix(), canonicalStep);
    }

    private CanonicalStepResult canonicalizeUCUMConcept(Concept concept, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        return switch (concept) {
            case UCUMPrefix ucumPrefix -> composeConsideringSpecial(canonicalStep, ucumPrefix.value().conversionFactor());
            case BaseUnit baseUnit -> new CanonicalStepResult(SoloTermBuilder.builder().withoutPrefix(baseUnit).noExpNoAnnot().asTerm().build(), PreciseDecimal.ONE, PreciseDecimal.ONE, false, null);
            case DerivedUnit derivedUnit -> canonicalizeDerivedOrDimlessUnit(derivedUnit, canonicalStep, substanceMolarMassCoeff);
            case DimlessUnit dimlessUnit -> canonicalizeDerivedOrDimlessUnit(dimlessUnit, canonicalStep, substanceMolarMassCoeff);
            case SpecialUnit specialUnit -> canonicalizeSpecialUnit(specialUnit, canonicalStep, substanceMolarMassCoeff);
            case ArbitraryUnit arbitraryUnit -> throw new TermHasArbitraryUnitException(arbitraryUnit);
        };

    }

    private CanonicalStepResult canonicalizeSpecialUnit(SpecialUnit specialUnit, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        Term sourceDef = registry.getDefinedUnitSourceDefinition(specialUnit, substanceMolarMassCoeff != null);
        CanonicalStepResult inner = canonicalizeImpl(sourceDef, new CanonicalStepResult(canonicalStep.term(), canonicalStep.magnitude(), canonicalStep.cfPrefix(), false, null), substanceMolarMassCoeff);
        return new CanonicalStepResult(
            inner.term(),
            inner.magnitude().multiply(specialUnit.value().function().value()),
            canonicalStep.magnitude(),
            true,
            specialUnit.value().function()
        );
    }

    private CanonicalStepResult canonicalizeDerivedOrDimlessUnit(DefinedUnit definedUnit, CanonicalStepResult canonicalStep, PreciseDecimal substanceMolarMassCoeff)
        throws TermHasArbitraryUnitException {
        CanonicalStepResult inner = canonicalizeImpl(registry.getDefinedUnitSourceDefinition(definedUnit, substanceMolarMassCoeff != null), canonicalStep, substanceMolarMassCoeff);
        return composeConsideringSpecial(
            inner,
            definedUnit.code().equals("mol") && substanceMolarMassCoeff != null ? substanceMolarMassCoeff : definedUnit.value().conversionFactor()
        );
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
                yield switch (unit) {
                    case SimpleUnit simpleUnit -> SoloTermBuilder.builder().withoutPrefix(simpleUnit.ucumUnit()).asComponent().withExponent(existingExponent).withoutAnnotation().asTerm().build();
                    case IntegerUnit integerUnit -> SoloTermBuilder.builder().withIntegerUnit(integerUnit.value()).asComponent().withExponent(existingExponent).withoutAnnotation().asTerm().build();
                };
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
                canonicalStepResult.specialFunction()
            );
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

    /**
     * Contains information about the canonicalization.
     */
    public sealed interface CanonicalizationResult {}

    /**
     * Represents a failed canonicalization. The subclasses provide more details.
     */
    public sealed interface FailedCanonicalization extends CanonicalizationResult {}

    /**
     * The canonicalization was successful.
     * @param magnitude The conversion factor that was created during the canonicalization.
     * @param canonicalTerm The canonical form of the given input term.
     */
    public record Success(PreciseDecimal magnitude, CanonicalTerm canonicalTerm) implements
        CanonicalizationResult {}

    /**
     * The canonicalization failed because the input term contains an arbitrary unit. Arbitrary units cannot be
     * converted to or from anything.
     * @param arbitraryUnit The arbitrary unit that was encountered and caused the failure.
     */
    public record TermHasArbitraryUnit(ArbitraryUnit arbitraryUnit) implements
        FailedCanonicalization {}

    /**
     * The canonicalization failed because the input term is the special unit '[pH]' and it's trying to be converted
     * to mass and mol to mass conversion is enabled in the configuration. This is just not supported (and this conversion
     * does not make any sense either).
     */
    public record TermContainsPHAndCanonicalizingToMass() implements FailedCanonicalization {}

}
