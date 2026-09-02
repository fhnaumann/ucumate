# ucumate

Python bindings for [ucumate](https://github.com/fhnaumann/ucumate) — a library for
working with [UCUM](https://ucum.org/) (Unified Code for Units of Measure):
validation, canonicalization, conversion, commensurability checks, and printing.

## Requires Java 21 or newer

**This package embeds the ucumate Java library and starts a JVM in-process via
[JPype](https://jpype.readthedocs.io/).** A Java 21+ runtime must be installed and
discoverable (via `JAVA_HOME` or on `PATH`) — this cannot be expressed as a Python
dependency, so it is not installed for you.

Check what you have:

```bash
java -version
```

The bundled JAR is a multi-release JAR that runs on any JDK from 21 upwards.

## Install

```bash
pip install ucumate
```

The wheel ships the shaded ucumate JAR, so there is nothing else to download.

## Usage

```python
from ucumate import UCUMService, ValidationSuccess, CanonicalizationSuccess

svc = UCUMService()

# Validate
svc.validate_to_bool("mg/dL")          # True
svc.validate_to_bool("not_a_unit!!!")  # False

result = svc.validate("mm[Hg]")
if isinstance(result, ValidationSuccess):
    print(result.term_string)

# Canonicalize
c = svc.canonicalize("mg/dL")
if isinstance(c, CanonicalizationSuccess):
    print(c.canonical_term, c.magnitude)   # g.m-3 10

# Convert: solves  factor * from = x * to
conv = svc.convert("g", "kg", factor="500")
print(conv.conversion_factor)              # 0.5

# Relation
svc.check_relation("m", "km")              # RelationIsCommensurable
svc.check_commensurable("m", "s")          # RelationNotCommensurable

# Print
from ucumate import PrintType
svc.print("kg.m/s2", PrintType.LATEX_SYNTAX)
```

### Result types

Every operation returns a typed result object rather than raising — mirroring the
sealed interface hierarchy in the Java library. Check with `isinstance`:

| Operation | Success | Failure variants |
|---|---|---|
| `validate` | `ValidationSuccess` | `ValidationFailure` (`.messages`) |
| `canonicalize` | `CanonicalizationSuccess` (`.canonical_term`, `.magnitude`) | `CanonicalizationFailedParserError`, `CanonicalizationFailedArbitraryUnit` (`.arbitrary_unit`), `CanonicalizationFailedPHWithMass` |
| `convert` | `ConversionSuccess` (`.conversion_factor`) | `ConversionFailedParserError`, `ConversionFailedBaseDimensionMismatch`, `ConversionFailedCanonicalization` |
| `check_relation` | `RelationIsEqual`, `RelationIsCommensurable`, `RelationNotCommensurable` | `RelationFailure`, `RelationParserError` |

Numeric values (`magnitude`, `conversion_factor`) are returned as **strings**. They
come from ucumate's arbitrary-precision `PreciseDecimal`, and converting to `float`
would silently discard precision. Wrap them in `decimal.Decimal` if you need
arithmetic.

### Known issue in 1.0.8

Some inputs with a zero denominator — `/0mg` is a confirmed case — pass `validate()`
and then let a raw Java `ArithmeticException: Division by zero` escape from
`canonicalize()` instead of returning a `CanonicalizationFailed*` result. If your
input set can contain these, guard the call:

```python
try:
    c = svc.canonicalize(term)
except Exception:
    c = None  # treat as non-canonicalizable
```

### JVM configuration

The JVM starts lazily on the first `UCUMService()` construction and is shared for the
lifetime of the process. To point at a different JAR (for example a locally built
snapshot), set `UCUMATE_JAR` to its full path before the first call.

## Versioning

The Python package version tracks the `ucumate-core` version it bundles, so
`ucumate==1.0.8` on PyPI contains `io.github.fhnaumann:ucumate-core:1.0.8`.

## License

Apache-2.0. See [LICENSE](LICENSE).
