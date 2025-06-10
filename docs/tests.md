---
order: 70
---
# UCUM Tests

Having a full testsuite to define accordance with the UCUM standard is important. The testsuite should cover a range of
valid and invalid expressions and cover canonicalization and conversion including the resulting factors.
Grahame Grieve maintains a [functional XML testsuite](https://ucum.org/docs/functional-tests) that covers a lot of test
cases. 

ucumate passes almost all (see section below) the validation and conversion tests defined by Grahame, the other test cases were not tested.

As part of developing the ucumate library, a new test suite has been developed. It is an accumulation of common UCUM codes
together with extensive an extensive range of UCUM codes to cover edge cases.

* [Common UCUM codes in FHIR](https://www.hl7.org/fhir/R5/valueset-ucum-common.html)
* [Common UCUM units from UCUM](https://ucum.org/docs/common-units)
* Custom tests

They were transformed into a single json file.

Note: The new testsuite is not meant to replace the XML testsuite (for now). The tests from the older testsuite are not
all present in the JSON testsuite.

The `core` library fails some conversion tests due to limited precision when working with special units. The `ucumate-core-exact-special-math` implementation
passes all tests, so use that if you require high precision.

[!file JSON Testsuite](static/ucum-tests.json)

## Conformance to the UCUMFunctionTestsXML

All tests pass with these exceptions listed below. They are manually marked to be skipped during a test run.

Most of these problems arise from the precision and rounding decisions made in both implementations. Read more about
the encountered problems and decisions made regarding conversion factor precision [here](precision.md).

* 1-108: `10+3/ul` is expected to be invalid but ucumate says it's valid. In ucumate any positive integer unit may have an exponent.
* 3-115: `6.3` * `s/4/m` = `1.6` * `s/m`. ucumate is more precise and does not round in this case. It returns `1.575`.
* 3-121: 1 * `10*-7.s` = `1e-7` * `s`. Just a string mismatch, ucumate returns `0.0000001` instead of `1e-7`
* 3-122: 1 * `4.[pi].10*-7.s` = `0.00000125663706143591729538506` * `s`. ucumate is more precise by default and returns
`0.00000125663706143591729538505735331180115367886776`. The last digit (`6`) is rounded and causes the problem.
You can configure the desired precision through the `PreciseDecimal` class.
* 3-123: Same problem as 3-122.
* 3-124: 1 * `[mu_0]` = `0.00125663706143591729538506` * `g.m.C-2`. ucumate is more precise by default returns
`0.00125663706143591729538505735331180115367886775975`.
* 3-128: 1 * `1/[ly]` = `1.05700083402461546370946e-18` `cm-1`. ucumate is more precise and does not output conversion
factors in scientific notation. It returns `0.00000000000000000105700083402461546370946052448513`.