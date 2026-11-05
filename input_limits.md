# Input Limits

Some inputs are valid UCUM syntax but take very long to canonicalize or convert, i.e. `10*999999` or `[pi]99999`.
Very long or deeply nested units (i.e. thousands of parentheses) can also cause a `StackOverflowError`. If you process
unit codes from external sources (i.e. a FHIR validation server), you probably want to reject these.

By default, `ucumate` rejects such inputs before processing them. The default limits are far above anything found in
common UCUM codes.

Below are the properties to control the limits.

| Property Name                   | Default Value | Description                                                                                                  |
|---------------------------------|---------------|--------------------------------------------------------------------------------------------------------------|
| ucumate.limits.enable           | true          | Enables/Disables the Limits                                                                                  |
| ucumate.limits.maxUnitLength    | 1000          | Max length of a unit. Text inside annotations (`{...}`) is not counted                                       |
| ucumate.limits.maxTotalExponent | 300           | Max sum of all exponents in a unit (ignoring the sign). A unit without an exponent counts as 1, i.e. `m2/s` is 3 |
| ucumate.limits.maxValueDigits   | 1000          | Max number of digits of a value, i.e. `5e3` (= `5000`) has 4 digits                                          |

!!!info Thread Stack Size

The default `maxUnitLength` works with the default JVM thread stack size (1 MB). If you process units on threads with a
smaller stack, lower `maxUnitLength` as well.

!!!

If an input exceeds a limit, `validate` returns a `Failure` (and `validateToBool` returns `false`). `canonicalize`,
`convert`, `checkRelation` and `checkCommensurable` return an `InputLimitExceeded` containing the reason.

### Property File

```properties
# Inside a file called ucumate.properties
ucumate.limits.enable=true
ucumate.limits.maxUnitLength=1000
ucumate.limits.maxTotalExponent=300
ucumate.limits.maxValueDigits=1000
```

### Builder

```java
Configuration configuration = Configuration.builder()
        .inputLimits(new InputLimits(true, 1000, 300, 1000))
        .build();
ConfigurationRegistry.initialize(configuration);
```

### Per UCUMService

You can also set the limits for a single `UCUMService`. This overrides the global configuration for that instance.

```java
UCUMService service = new UCUMService(); // uses the global configuration
UCUMService unlimitedService = new UCUMService(UcumVersion.V2_2, InputLimits.unlimited());
```

To turn the limits off completely, set `ucumate.limits.enable=false` or use `InputLimits.unlimited()`.
