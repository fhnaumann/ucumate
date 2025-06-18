# Feature Flags

The UCUM specification has some ambiguity, and therefore it is not easy to agree on what might be valid UCUM syntax and what not.
`ucumate` provides some feature flags that let the developer choose by which rules to play by.

## Setting Feature Flags

To enable or disable these settings application wide, you can use properties of the provided builder class.

### Properties

Create a `ucumate.properties` file in your resource folder.

```properties
ucumate.enablePrefixOnNonMetricUnits=true
ucumate.enableMolMassConversion=true
ucumate.allowAnnotAfterParens=true
```

### Builder

```java
Configuration configuration = Configuration.builder()
        .enablePrefixOnNonMetricUnits(true)
        .enableMolMassConversion(true)
        .allowAnnotAfterParens(true)
        .build();
ConfigurationRegistry.initialize(configuration);
```


### Prefixes on Non-Metric Units

The UCUM specification mentions that prefixes (i.e. `centi`, `mili`, ...) may only be used on metric units. For example,
`meter` may be prefixed with `centi` to form `centimeter` (UCUM syntax: `cm`). Forming `centifoot` (UCUM syntax: `c[ft_i]`)
would be invalid. Sometimes this behaviour is desired and therefore this flag exists.

!!!info The `da` Problem

If prefixes on non-metric units are disabled, then `da` matches the `deka` prefix which just by itself is invalid UCUM syntax.
If enabled, then `da` matches `deci year`.

!!!

### Annotations on Parenthesis

It is not clear whether `(m.s){annot}` is valid or not. Some common UCUM code lists include such codes, hence this flag exists.

### Mol <-> Mass Conversion

The UCUM specification explicitly states that mol (and any unit defined on it) canonicalizes to avogadro's number (dimensionless).
This is sometimes undesirable as in different context it can also canonicalize to a mass (usually gram). This requires an
additional parameter, the substance's molar mass coefficient.