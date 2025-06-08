---
order: 100
---
# ucumate

ucumate is a developer-friendly Java library for parsing, validating, canonicalizing, and converting units of measure 
based on the [Unified Code for Units of Measure (UCUM)](https://ucum.org/) standard. It provides validation, canonicalization, and
conversion support with high decimal precision if desired. Furthermore, all *special units* are supported. 

This library requires Java 21 to run.
Currently, all dependencies are hosted through jitpack so you need add their repository in the `pom.xml`.

```xml
    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

You will need the core implementation.

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>v1.0.0</version>
</dependency>
```

If you need high precision (especially when special units are involved) then you should use the [ucumate-core-exact-special-math module](exact_math.md).

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core-exact-special-math</artifactId>
    <version>v1.0.0</version>
</dependency>
```

If you need data persistence across restarts you can use the [ucumate-persistence](persistence.md) module.

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-persistence</artifactId>
    <version>v1.0.0</version>
</dependency>
```
