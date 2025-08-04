# Error Feedback on Validation

Add the dependency:

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-error-feedback</artifactId>
    <version>1.0.5</version>
</dependency>
```

This will automatically inject the `FeedbackValidator` into the `UCUMService`.

The new validator tries to generate error messages with details on the invalid parts.
Below is a table containing common errors that will be captured, analyzed, and explained in the error message.

| Error Type                  | Details                                                                                                                                                                                                                                                                 |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Spaces Anywhere             | UCUM does not allow spaces in expressions.                                                                                                                                                                                                                              |
| Wrong Multiplication Symbol | `*` can be misused as the multiplication symbol while only `.` is allowed.                                                                                                                                                                                              |
| Wrong Division Symbol       | Albeit less common, `\` can be misused as the division symbol.                                                                                                                                                                                                          |
| Wrong Exponentiation Symbol | UCUM does not have a direct exponentiation symbol. Instead they just follow on units with an optional sign.                                                                                                                                                             |
| Unknown Unit                | The provided unit does not match a known UCUM Unit code. It will try to match (in that order) <ul><li>The same unit but with square brackets, i.e. `ft_i` matches `[ft_i]`</li><li>The case-insensitive code</li><li>The print symbol</li><li>The other names</li></ul> |
| Missing `(` or `)`          | Detects unopened or unclosed parenthesis.                                                                                                                                                                                                                               |
| Missing `[` or `]`          | Detects unopened or unclosed square brackets in units.                                                                                                                                                                                                                  |
| Missing `{` or `}`          | Detects unopened or unclosed curly brackets in unit annotations.                                                                                                                                                                                                        |
| Negative Integer Number     | Negative integer numbers are not allowed in UCUM. Only positive integers numbers are.                                                                                                                                                                                   |
| Special Unit with Division  | Special units together with division are not allowed. Special units may only use integer multiplication or prefixes.                                                                                                                                                    |
| Special Unit with Exponent  | Special units are not allowed to have an exponent. Special units may only use integer multiplication or prefixes.                                                                                                                                                       |

!!!info Automatically Fix Errors
Currently, the validator only provides detailed error messages, but it does not correct anything.
Please [get in contact](https://github.com/fhnaumann/ucumate/issues/new) if there is need for a validator that tries to automatically fix invalid inputs.
!!!