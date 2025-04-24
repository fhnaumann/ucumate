package org.example.util;

public class Constants {

    /*
    Per definition, UCUM does not allow annotations after an expression that is wrapped
    in parentheses (see Backus Naur Form). But in text form, these two rules exist:
    2.4 §12 1. "Curly braces may be used to enclose annotations that are often written in place of units or behind units [...]"
    and
    2.2 §10 1. "Unit terms with operators may be enclosed in parentheses (‘(’ and ‘)’) and used in place of simple units."

    The second rule indicates that any (valid) expression can be wrapped in parentheses, and it will be treated
    as a simple unit. This would mean (m.s){annot} (or even (m){annot}) would be valid.
     */
    public static final boolean ALLOW_ANNOT_AFTER_PARENS = false;

    /*
    Per definition, UCUM does not allow prefixes before non-metric units. But most practical
    implementations allow it and just apply the prefix during the canonicalization.
    2.3 §11 1. "Only metric unit atoms may be combined with a prefix."
     */
    public static final boolean ALLOW_PREFIX_ON_NON_METRIC_UNITS = true;

    /*
    Most math is covered by the PreciseDecimal class to handle correct rounding, precision and scale.
    It is hard to cover the mathematical operations that are necessary to calculate special unit conversion.
    For now, the scale is fixed to 4 which should be enough in most cases. If *exact* conversion is necessary,
    then this flag can be set to false. Then an external (much slower) math library will be used to calculate
    the special conversion factors *exactly*.
    The math library is https://github.com/eobermuhlner/big-math and has to be supplied by the developer using
    this UCUM library.
     */
    public static final boolean EXPRESSIONS_WITH_SPECIAL_UNITS_LIMITED_TO_4_SCALE = true;
}
