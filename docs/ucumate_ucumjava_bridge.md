# Ucum-Java Legacy Support

ucumate offers a drop-in replacement module for [Ucum-java](https://github.com/FHIR/Ucum-java).

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-ucumjava-bridge</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

You can construct a `UcumateToUcumJavaService` which implements most of the functionality from the `Ucum-java` interface definition.

Below you find a detailed description on the differences to the old `Ucum-java` implementation.

## General Differences

!!!warning Error Feedback

`Ucum-java` provides some basic feedback when an action failed. I.e. if the given input is invalid, it will return a string
message with details. If the input is valid, it returns null. The `ucumate-ucumjava-bridge` implementation does not return
(the same) string error messages. You should only rely on `== null` or `!= null` checks but not on the string contents itself.
!!!


| Method                 | ucum-java                                                     | ucumate-ucumjava-bridge                                                                                                                  |
|------------------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| validateInProperty     | Validate unit with specific property requirement              | Not Implemented                                                                                                                          |
| validateUCUM           | A list of special units because they are not implemented here | An empty list, everything is implemented and supported                                                                                   |
| analyse                | A string representation of the parsed input expression        | The same output as `ucum-java` but it prints a warning when parenthesis ambiguity is encountered                                         |
| validateCanonicalUnits | Validate Canonical Units with potential error message         | The same as `ucum-java` but different error messages                                                                                     |
| validate               | Validate an input                                             | In principle the same as `ucum-java` but there are some [key differences in validity](tests.md#conformance-to-the-ucumfunctiontestsxml). |
| multiply               | Multiply two expressions                                      | Not Implemented                                                                                                                          |
| divideBy               | Divide two expressions                                        | Not Implemented                                                                                                                          |

## Functional XML Test Difference

Please see the [tests page](tests.md#conformance-to-the-ucumfunctiontestsxml) for the difference between `ucum-java` and `ucumate`. The `ucumate-ucumjava-bridge` does change any validation or conversion logic
compared to the core implementation.

## Functional JSON Test Difference

During the development of ucumate, a new additional [test suite](tests.md) has been created. This adds more extensive edge case tests,
but it also has some slightly different validation rules which will be highlighted below.

Here are all the test cases from the JSON tests where `ucum-java` differs from `ucumate`. The `ucumate-ucumjava-bridge` does change any validation or conversion logic
compared to the core implementation.

| Test ID | Input (Diplay) | Expected | Ucum-java (Actual) | Reason                                                                                                                                                                                                                                                                                     |
|---------|----------------|----------|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 32      | da             | Valid    | Invalid            | `da` is parsed as the single prefix (correctly marked as invalid). But it can also be parsed as `dezi * year` (if non-metric units are allowed to have prefixes).                                                                                                                          |
| 52      | m\{\{\}        | Invalid  | Valid              | The UCUM spec states 'Curly brackets may not be nested' but includes `{` and `}` as valid symbols inside an annotation. This is ambiguous and there is no use case for this, so ucumate just disallows this. Also see [this discussion](https://github.com/orgs/ucum-org/discussions/380). |
| 55      | m\{ \}         | Invalid  | Valid              | The space character is number 32, but only characters inside the range 33-126 may be used.                                                                                                                                                                                                 |
| 66      | 2+5            | Valid    | Invalid            | `Ucum-java` does not allow integer units to be raised to an exponent. This is ambiguous in the UCUM spec but there is no real disadvantage to supporting it, so `ucumate` allows it.                                                                                                       |
| 67      | 12+5           | Valid    | Invalid            | `Ucum-java` does not allow integer units to be raised to an exponent. This is ambiguous in the UCUM spec but there is no real disadvantage to supporting it, so `ucumate` allows it.                                                                                                       |
| 68      | 10+5           | Valid    | Invalid            | `Ucum-java` does not allow integer units to be raised to an exponent. This is ambiguous in the UCUM spec but there is no real disadvantage to supporting it, so `ucumate` allows it.                                                                                                       |
| 69      | 10-5           | Valid    | Invalid            | `Ucum-java` does not allow integer units to be raised to an exponent. This is ambiguous in the UCUM spec but there is no real disadvantage to supporting it, so `ucumate` allows it.                                                                                                       |
| 71      | -1             | Invalid  | Valid              | Negative integers are not allowed in UCUM but `ucum-java` allows it.                                                                                                                                                                                                                       |
| 71      | -5.2           | Invalid  | Valid              | Negative integers are not allowed in UCUM but `ucum-java` allows it.                                                                                                                                                                                                                       |
| 72      | -5\{abc\}      | Invalid  | Valid              | Negative integers are not allowed in UCUM but `ucum-java` allows it.                                                                                                                                                                                                                       |
| 73      | /Cel           | Invalid  | Valid              | Special units may not be part of a division.                                                                                                                                                                                                                                               |
| 74      | Cel/3          | Invalid  | Valid              | Special units may not be part of a division.                                                                                                                                                                                                                                               |
| 75      | Cel/m          | Invalid  | Valid              | Special units may not be part of a division.                                                                                                                                                                                                                                               |
| 76      | Cel/Cel        | Invalid  | Valid              | Special units may not be part of a division. (**See Note below**)                                                                                                                                                                                                                          |
| 82      | Cel+2          | Invalid  | Valid              | Special units may not have an exponent.                                                                                                                                                                                                                                                    |
| 83      | Cel2           | Invalid  | Valid              | Special units may not have an exponent.                                                                                                                                                                                                                                                    |
| 84      | Cel-2          | Invalid  | Valid              | Special units may not have an exponent.                                                                                                                                                                                                                                                    |
| 85      | m.5.Cel2.s.2   | Invalid  | Valid              | Special units may not have an exponent.                                                                                                                                                                                                                                                    |
| 86      | m.5.Cel2.s/2   | Invalid  | Valid              | Special units may not have an exponent and may not be part of a division.                                                                                                                                                                                                                  |

!!!warning Normalizing with Special Units

Special units are not allowed to be part of an expression that also contains a division. I.e. `Cel/m` is invalid. Now it is unclear how this
behaves in regard to expression normalization. I.e. `m/m` canonicalizes to `1`. Should therefore `Cel/Cel` normalize to `1` and be counted
as a valid UCUM expression? Currently, this is not the case in ucumate and it will show `Cel/Cel` as invalid.
**Please get in contact if you have an opinion on this.**

!!!