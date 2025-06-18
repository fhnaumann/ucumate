# Exact Math

If you work with special units and need high precision during canonicalization or conversion then you should use this additional module.

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core-exact-special-math</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

It is meant as a drop-in replacement for the default math precision in `ucumate-core`. On startup, you have set the provider once.

```java
SpecialUnits.setProvider(new PreciseSpecialUnitsFunctionProvider());
```

Under the hood it uses [big-math](https://github.com/eobermuhlner/big-math) to perform the complex math operations that
are necessary when working with special units. It is much slower than the native but imprecise one. This implementation
does not suffer from the floating point precision error that may occur in some canonicalizations or conversions.