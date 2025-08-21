# ucumate

ucumate is a developer-friendly Java library for working with units of measure based on the [UCUM standard](https://ucum.org/). At its core, 
it provides validation, canonicalization, conversion, and printing support with automatic caching. Optional features 
enable persistent database storage, mole-to-mass conversions, and high-precision calculation of conversion factors 
for special units. 

An [online demo](https://virtuous-respect-production.up.railway.app/) is also available.

```xml
<dependency>
    <groupId>com.github.fhnaumann.ucumate</groupId>
    <artifactId>ucumate-core</artifactId>
    <version>1.0.6</version>
</dependency>
```

```java
UCUMService ucumService = new UCUMService(); // default version is 2.2, but you can also specify a different version
LookupResult lookupResult = ucumService.lookup("m");
String print = ucumService.print("cm");
ValidationResult valResult = ucumService.validate("cm");
CanonicalizationResult canonResult = ucumService.canonicalize("[in_i]");
ConversionResult convResult = ucumService.convert("[ft_i]", "[in_i]");
```

Read more about the usage in the [documentation](https://fhnaumann.github.io/ucumate/).

Try out the [live demo](https://virtuous-respect-production.up.railway.app/).

# Build process

1. Build docks, retype build. Copy .retype into demo/src/resources/doc
2. Build frontend, npm build. Copy /dist into demo/src/resources
3. Deploy demo, railway up (builds docker image)

# License

This project includes data from [NistChemData](https://github.com/IvanChernyshov/NistChemData),
licensed under the MIT License.
Copyright © 2023 Ivan Chernyshov.