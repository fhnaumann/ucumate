---
order: 70
---
# UCUM Tests

Having a full testsuite to define accordance with the UCUM standard is important. The testsuite should cover a range of
valid and invalid expressions and cover canonicalization and conversion including the resulting factors.
Grahame Grieve maintains a [functional XML testsuite](https://ucum.org/docs/functional-tests) that covers a lot of test
cases. 

ucumate passes all the validation and conversion tests defined by Grahame, the other test cases were not tested.

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