---
order: 100
---
# ucumate

ucumate is a developer-friendly Java library for working with units of measure based on the [UCUM standard](https://ucum.org/). At its core,
it provides validation, canonicalization, conversion, and printing support with automatic caching. Optional features
enable persistent database storage, mole-to-mass conversions, and high-precision calculation of conversion factors
for special units.

An [online demo](https://virtuous-respect-production.up.railway.app/) is also available.

You will need the core implementation. This library requires Java 21 to run.

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>1.0.8</version>
</dependency>
```

If you used [Ucum-java](https://github.com/FHIR/Ucum-java) previously, you can use the [drop-in replacement module](ucumate_ucumjava_bridge.md).

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-ucumjava-bridge</artifactId>
    <version>1.0.8</version>
</dependency>
```

If you want error analysis with detailed messages on invalid input, use the [error feedback module](error_feedback.md).

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-error-feedback</artifactId>
    <version>1.0.8</version>
</dependency>
```


If you need high precision (especially when special units are involved) then you should use the [ucumate-core-exact-special-math module](exact_math.md).

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-core-exact-special-math</artifactId>
    <version>1.0.8</version>
</dependency>
```

If you need data persistence across restarts you can use the [ucumate-persistence module](persistence.md) .

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-persistence</artifactId>
    <version>1.0.8</version>
</dependency>
```

If you work with mol and mass units a lot you can use the optional [ucumate-mol module](mol_mass_conversion.md).

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-mol-support</artifactId>
    <version>1.0.8</version>
</dependency>
```
