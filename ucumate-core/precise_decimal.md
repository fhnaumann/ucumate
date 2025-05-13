# Details on the rounding behaviour with numbers provided by the ucum-essence file

## Definitions

*Significant digits*:

Digits that convey important information regarding the number they are part of.
There are three exceptions where a digit may be present but not considered significant:
- Leading zeros. I.e. 0.056m has two insignificant digits (the two 0's at the beginning) because the expression
may be scaled with a prefix which results in 56mm. Note that this is only the case when the digit before the
decimal point is 0.
- Trailing zeros (but not in UCUM). In physical measurements they may indicate that a measurement is approximated.
In the context of UCUM this is never the case. Here trailing zeros are always understood as intentional and are considered significant.
- Spurious digits. If through some way or another the resulting number has more significant figures than any input
number, then these additional digits have "false precision" and should be discarded.

[Identify significant figures](https://en.wikipedia.org/wiki/Significant_figures#Identifying_significant_figures)

**Precision**: 

The number of significant digits (ignoring leading zeros).

**Scale**:

The number of digits after the decimal point (ignoring leading zeros).

**Integer Number**:

Integer numbers are considered to have unlimited precision.

**Conversion Factor**:

In UCUM there are lots of conversion factors defined that are not integer numbers. For example, the conversion factor
from inches to cm is defined as 2.54. This is a decimal number and following the definitions above, it should have a
precision of 3 and a scale of 2. However, in the context of unit conversion, there is no ambiguity. The conversion factor
between these two units is *exactly* 2.54, there is no uncertainty. There is an exception to this when the conversion
factor involves a unit that is inherently irrational like pi or the gravitational constant.

## Scientific Notation

The ucum-essence file uses the scientific notation for many of the conversion factor definitions. For example,
the inch to cm conversion factor (2.54) is actually defined as 254e-2. This non-normalized form of the scientific notation
can be problematic. 254 is an integer but the normalized or interpreted form is a decimal number with limited precision.
In the conversion factor definitions this is not a problem because they are considered to be exact anyway (with a few exceptions)

## User-provided Factor

When canonicalizing or converting between units, the user may provide an additional factor that is used during the calculations.
For example, the user might want to convert 3.5 inches to cm. The system is incapable of determining whether this custom
factor has limited precision/scaling or not. It depends on what the user wants and therefore (maybe) I can add an additional
bool parameter to flag this behaviour?

## Examples

0 -> unlimited
3 -> unlimited
2.54 (ucum cf) -> unlimited
254e-2 (ucum cf) -> unlimited

1.0 -> p: 2, s: 1
1.00 -> p: 3, s: 2
0.01 -> p: 1, s: 1
0.0100 -> p: 3, s: 3

3.1415926535897932384626433832795028841971693993751058209749445923 (ucum def) -> p: ..., s: p-1
