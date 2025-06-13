# ucumate

Ucumate is a developer-friendly Java library for parsing, validating, canonicalizing, 
and converting units of measure based on the Unified Code for Units of Measure (UCUM) standard.
It provides validation, canonicalization, and conversion support with high decimal precision if desired. 
Furthermore, all special units are supported.

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>1.0.2-SNAPSHOT</version>
</dependency>
```

```java
String print = UCUMService.print("cm");
Validator.ValidationResult valResult = UCUMService.validate("cm");
Canonicalizer.CanonicalizationResult canonResult = UCUMService.canonicalize("[in_i]");
Converter.ConversionResult convResult = UCUMService.convert("[ft_i]", "[in_i]");
```

Read more about the usage in the [documentation](https://virtuous-respect-production.up.railway.app/doc/index.html).

# Build process

1. Build docks, retype build. Copy .retype into demo/src/resources/doc
2. Build frontend, npm build. Copy /dist into demo/src/resources
3. Deploy demo, railway up (builds docker image)

# License

This project includes data from [NistChemData](https://github.com/IvanChernyshov/NistChemData),
licensed under the MIT License.
Copyright © 2023 Ivan Chernyshov.