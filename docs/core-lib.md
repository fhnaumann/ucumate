---
order: 98
---
# Core Functionality

The ucumate library has these core features:

1. Lookup of UCUM expressions
2. Printing of UCUM expressions
3. Validation of UCUM expressions
4. Canonicalization of UCUM expressions
5. Conversion UCUM expressions

All functionality can be accessed through a single class: `UCUMService`. The library heavily uses sealed interface
together with switch pattern matching.

By default, the core library caches validation and canonicalization results. Therefore, any subsequent calls to the same
expressions lead to blazingly fast results. So the longer the app runs, the faster the answers on average get. 
You can [change these cache settings](cache.md) if desired. You can also add [persistent storage](persistence.md) if needed.

## Lookup

`UCUMService#lookup` can be used to look up properties of a UCUM expression.
UCUMExpression use the [validator result](core-lib.md#validation).

```java
LookupService.LookupResult lookupResult = ucumService.lookup(input);
String print = switch (lookupResult) {
    case NoMatch noMatch -> "Failed to match a unit";
    case DirectMatch directMatch -> "Direct match found!";
    case MultipleMatches multipleMatches -> "Multiple matches found!";
};
```

## Printing

Printing more detailed information about a given UCUM expression is very important. Currently, there are 4 print modes:
1. UCUM Syntax: The parsed UCUM expression is printed in valid UCUM syntax.
2. Expressive UCUM Syntax: Includes the human-readable name when printing a unit. May be used to understand how an expression has been parsed.
3. Common Math Syntax: Prints in UCUM Syntax but replaces the math operators with more commonly used ones (i.e. . -> *, + -> ^).
4. LaTeX Syntax: Produces a string that most LaTeX parsers should be able to process and render.

For example, `cm/(s.g2)` prints the following:
1. UCUM Syntax: `cm/(s.g+2)`
2. Expressive UCUM Syntax: `c (centi)m (meter)/(s (second).g (gram)+2)`
3. Common Math Syntax: `cm/(s*g^2)`
4. LaTeX Syntax: `\frac{\mathrm{cm}}{\left(\mathrm{s} \cdot \mathrm{g}^{2}\right)}` which renders to ![rendered_latex](static/latex_example_img.png)

The default printing mode is the UCUM syntax. It is accessible through the `UCUMService#print` method.

```java
System.out.println(ucumService.print("cm")); // cm
// or when you have a parsed term object
Term term = ...;
System.out.println(ucumService.print(term));
```

You may also specify the print mode with another parameter.

```java
System.out.println(ucumService.print("cm", Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX)); // c (centi)m (meter)
```

## Validation

`UCUMService#validate(String input)` can be used to validate any given UCUM expression. The result is either a
`Success` containing the parsed `Term` or a `Failure`.

```java
ValidationResult valResult = ucumService.validate("cm.s");
String print = switch(valResult) {
    case Failure failure -> "Invalid input";
    case Success sucess -> ucumService.print(dimensionsMatch.term(), Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX);
};
System.out.println(print); // c (centi)m (meter).s (second)
```

If you are only interested whether a given UCUM expression is valid or invalid you may also use `UCUMService#validateToBool(String input)`.

```java
boolean valid = ucumService.validateToBool("cm.s");
System.out.println(valid); // true
```

## Canonicalization

`UCUMService#canonicalize` can be used to validate any given UCUM expression. The result is either a `Success` containing
the parsed `Term` and its canonicalization factor obtained during the canonicalization or a `FailedCanonicalization`.

```java
CanonicalizationResult canonResult = ucumService.canonicalize("[in_i]");
String print = switch (canonResult) {
    case FailedCanonicalization failedCanonicalization -> "Invalid input";
    case Success dimensionsMatch -> String.format("Canonicalization Factor: %s, Canonical Form: %s".formatted(dimensionsMatch.magnitude(), ucumService.print(dimensionsMatch.canonicalTerm())));
};
System.out.println(print); // Canonicalization Factor: 0.0254, Canonical Form: m+1
```

## Conversion

`UCUMService#convert` can be used to convert any given UCUM expression to another. The result is either a `Success` containing
the resulting conversion facor or a `FailedConversion`.

```java
ConversionResult convResult = ucumService.convert("[ft_i]", "[in_i]");
String print = switch (convResult) {
    case FailedConversion failedConversion -> "Conversion failed";
    case Success dimensionsMatch -> "1 [ft_i] is %s [in_i]".formatted(dimensionsMatch.conversionFactor());
};
System.out.println(print); // 1 [ft_i] is 12 [in_i]
```