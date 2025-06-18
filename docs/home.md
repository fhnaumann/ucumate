---
order: 100
---
# ucumate

ucumate is a developer-friendly Java library for parsing, validating, canonicalizing, and converting units of measure 
based on the [Unified Code for Units of Measure (UCUM)](https://ucum.org/) standard. It provides validation, canonicalization, and
conversion support with high decimal precision if desired. Furthermore, all *special units* are supported. 

This library requires Java 21 to run.
Currently, all dependencies are hosted on the maven central snapshot repository, you need to add it to your `pom.xml`

```xml
<repositories>
    <repository>
        <name>Central Portal Snapshots</name>
        <id>central-portal-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

You will need the core implementation.

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

If you used [Ucum-java](https://github.com/FHIR/Ucum-java) previously you can use the [drop-in replacement module](ucumate_ucumjava_bridge.md).

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-ucumjava-bridge</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```


If you need high precision (especially when special units are involved) then you should use the [ucumate-core-exact-special-math module](exact_math.md).

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core-exact-special-math</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

If you need data persistence across restarts you can use the [ucumate-persistence module](persistence.md) .

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-persistence</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

If you work with mol and mass units a lot you can use the optional [ucumate-mol module](mol_mass_conversion.md).

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-mol-support</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```
