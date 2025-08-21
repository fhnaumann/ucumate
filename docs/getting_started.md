---
order: 99
---
# Getting Started

## Add dependency

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>1.0.6</version>
</dependency>
```

## Core Functionality

Use the `UCUMService` class to access the core functionality. This includes lookup, printing, validation, canonicalization and conversion.

```java
UCUMService ucumService = new UCUMService(); // default version is 2.2, but you can also specify a different version
LookupResult lookupResult = ucumService.lookup("m");
String print = ucumService.print("cm");
ValidationResult valResult = ucumService.validate("cm");
CanonicalizationResult canonResult = ucumService.canonicalize("[in_i]");
ConversionResult convResult = ucumService.convert("[ft_i]", "[in_i]");
```

Read more about the parameter and return types of the `UCUMService` class in the [core functionality documentation](core-lib.md).

