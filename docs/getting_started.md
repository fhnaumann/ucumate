---
order: 99
---
# Getting Started

## Add dependency

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>v1.0.1</version>
</dependency>
```

## Core Functionality

Use the `UCUMService` class to access the core functionality. This includes printing, validation, canonicalization and conversion.

```java
String print = UCUMService.print("cm");
Validator.ValidationResult valResult = UCUMService.validate("cm");
Canonicalizer.CanonicalizationResult canonResult = UCUMService.canonicalize("[in_i]");
Converter.ConversionResult convResult = UCUMService.convert("[ft_i]", "[in_i]");
```

Read more about the parameter and return types of the `UCUMService` class in the [core functionality documentation](core-lib.md).

