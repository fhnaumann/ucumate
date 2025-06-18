# Mole <-> Mass Conversion

In UCUM the `mol` unit is defined as dimensionless, more precisely `1 mol = 6.02214076*10^23`. Here is their reasoning:

> The rationale for removing the mole from the base is that the mole is essentially a count of particles expressed in a 
> unit of very high magnitude (Avogadro's number). There is no fundamental difference between the count of particles 
> and the count other things. 

ucumate supports both conversions so depending on the provided context:

* `1 mol = 6.02214076*10^23`
* `1 mol = X * g` where `X` is a provided substance's molar mass coefficient which changes depending on the solution.

There is a property `ucumate.enableMolMassConversion` which can be used to allow or disallow the conversion application-wide.
It is allowed by default.

!!!warning Warning
Enabling this property just allows the `mole <-> mass` conversion in general. **It does not automatically convert every
encountered `mol` unit to `g`**. You have to explicitly enable this on a per-conversion basis. On the other hand, disabling
the property completely prohibits the conversion, and it's not possible to enable it on a per-conversion basis. See below
on how to do that.
!!!

Assuming the `ucumate.enableMolMassConversion` property is enabled, you can convert `mol` (or any unit that canonicalizes to mol)
to a mass unit.

```java
UCUMService.convert("1", "mol", "g", "18.01528"); // 18.01528 is the substance coefficient in water
```

If you don't provide the substance coefficient, then the conversion will fail because ucumate will try to convert to the
dimensionless unit.

```java
UCUMService.convert("1", "mol", "g"); // yields a BaseDimensionMismatch because mol->g without substance coefficient is not possible
UCUMService.convert("1", "mol", "1"); // yields 6.02214076*10^23
```

Conversely, if `ucumate.enableMolMassConversion` is set to `false`, then converting mole to gram will always fail

```java
UCUMService.convert("1", "mol", "g", "18.01528"); // yields a BaseDimensionMismatch because mol<->mass conversion is disabled application-wide
```

### Substance's Molar Mass Values

If you don't want to provide decimal number for the substance's molar mass coefficient you can add the `ucumate-mol-support` dependency.

```xml
<dependency>
    <groupId>io.github.fhnaumann</groupId>
    <artifactId>ucumate-mol-support</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

This lets you define the coefficient optionally by a different `name`, `casNr`, `formular`, etc.

```java
// Use a casNr instead of a decimal number.
Converter.ConversionResult result = UCUMService.convert("1", "mol", "g", "1309-37-1");
System.out.println(result); // conversionFactor=159.688
```

### Special `[pH]` unit

The `[pH]` unit is unique in the sense that it is the only unit defined in UCUM that is a special unit and defined on the mole unit.
So if mole <-> mass conversion is enabled, in theory it should be possible to also convert `[pH]` to a mass unit. But it is
unclear what this even means or in which context it would be used. Therefore, ucumate explicitly does not allow the conversion
of `[pH]` to a mass unit.

```java
// Assuming ucumate.enableMolMassConversion=true
UCUMService.convert("1", "[pH]", "g", "18.01528"); // yields a TermContainsPHAndCanonicalizingToMass failure result
UCUMService.convert("1", "[pH]", "/l"); // yields a Success
```

### Caching and Storing Behaviour

Caching or storing any canonicalization/conversion where the `mol` unit (or any unit defined on `mol`) is not supported. Caching these
calculations would require the provided substance's molar mass coefficient to be stored in the cache key as well which is not implemented
at the moment.