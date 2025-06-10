ucumate: A Java Library for Working with UCUM Expressions

Hi all,

I have developed a Java library that enables printing, validation, canonicalization, 
and conversion of [UCUM expressions](https://ucum.org/).

Try out the [online demo](https://go.csiro.au/FwLink/ucumate-demo)!

### What can it do?

- Print, validate, canonicalize and convert any UCUM expression
- Understand complex expressions with redundant parts, i.e. it understands that `m.m` = `m2`
- Add a cache over time that drastically improves the return speed when called
- Add a persistent storage layer (if desired)

### What are the limitations?

- Requires at least Java 21 to run
- It is not an exact implementation of the UCUM standard. For 99% of the use cases it conforms to the standard like all 
other existing implementations, but the UCUM standard itself has some ambiguity which results in unclear edge cases. 
Please report any unexpected behavior you encounter!

### What is the motivation?

My intentions are to improve the current situation regarding
UCUM support in the Java ecosystem (and FHIR in particular). The library solves the limitations the older 
[ucum-java](https://github.com/FHIR/Ucum-java) has and has full support for special units. It also has additional features
like caching, persistent database storage, and precise math calculations (for special units).

The project is being developed as part of a broader effort to enhance UCUM handling in Ontoserver, 
and it is planned to be integrated into Ontoserver soon. 
Until then, I’m working to resolve remaining bugs and gather community feedback.

# Demo

- A small demo app can be accessed at https://go.csiro.au/FwLink/ucumate-demo
- The usage documentation is available at https://fhnaumann.github.io/ucumate/ (JitPack is currently down...)
- The project source code is on GitHub at https://github.com/fhnaumann/ucumate
- Additionally, an interactive C4 documentation is available at https://s.icepanel.io/qSFYjsT1eKkVw5/36V0