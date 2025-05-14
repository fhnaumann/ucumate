# Details on the rounding behaviour with numbers provided by the ucum-essence file

## Problem

Conversion factors are usually exact numbers (even decimals) that have infinite precision. I.e. the conversion factor from
inches to cm is 2.54. It is a decimal number and in a different context it has a limited precision of 3. However, in the
context of unit conversion it has unlimited precision. The conversion factor is exactly that, no uncertainty.
There are a few exceptions to this. Some units rely on constants that are inherently limited when used in computers. Most
notably this includes any unit that relies on pi. To add to this mess, lately (~ 5 years) some constants like the planck
constant have been switched from being an inexact measurement to being defined as an exact number. They now have infinite
precision. UCUM does not provide a way to differentiate between them and therefore manually sorting and checking has to be
done once. 

Another problem is when the user provides an additional factor during unit conversion. For example, a user might want to
know how much 3.5 inches are in cm. Again, it's not possible for the system to directly know what the precision is.
Interpreting the 3.5 as a measurement would indicate that the resulting conversion factor (8.89 cm) should be rounded to
9.0 cm. If the input had been 3.50 then it results in 8.89 cm. Personally, I think its annoying for the user to rely on this
behaviour. Instead, a flag could be used where the user marks their input factor as exact with unlimited precision.

There is also the division problem. If two numbers with infinite precision are divided, the resulting number should have
infinite precision as well. But the division is often not exact and the result has to be cut to *some* precision. I.e. the
absolute max precision may be 100. The question is how this should be marked. Should the result now have limited precision (100)
or should it still be marked as infinite precision and just show the 100 digits.

Additionally, special units also use other more complex math functions. It is not feasible to implement them all with scientific
rounding. Therefore, another math lib (big-math) has to be used. In cases where really precise rounding together with
special units is needed, this lib would have to be used. 


## Definitions

**Significant digits**:

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
In the conversion factor definitions this is not a problem because they are considered to be exact anyway (with a few exceptions).

## User-provided Factor

When canonicalizing or converting between units, the user may provide an additional factor that is used during the calculations.
For example, the user might want to convert 3.5 inches to cm. The system is incapable of determining whether this custom
factor has limited precision/scaling or not. It depends on what the user wants and therefore (maybe) I can add an additional
bool parameter to flag this behaviour?

There is additional complexity when the factor is in scientific notation, especially regarding the decimal places after the dot.
1.0e2 expands to 100.0, 1.00e2 -> 100.00, etc. Conversely, 1.0e-2 expands to 0.010, 1.00e-2 -> 0.0100

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
