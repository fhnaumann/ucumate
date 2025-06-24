---
order: 99
---
# Getting Started

## Add dependency

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

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>1.0.3-SNAPSHOT</version>
</dependency>
```

## Core Functionality

Use the `UCUMService` class to access the core functionality. This includes lookup, printing, validation, canonicalization and conversion.

```java
UCUMService ucumService = new UCUMService();
LookupResult lookupResult = ucumService.lookup("meter");
String print = ucumService.print("cm");
ValidationResult valResult = ucumService.validate("cm");
CanonicalizationResult canonResult = ucumService.canonicalize("[in_i]");
ConversionResult convResult = ucumService.convert("[ft_i]", "[in_i]");
```

Read more about the parameter and return types of the `UCUMService` class in the [core functionality documentation](core-lib.md).

