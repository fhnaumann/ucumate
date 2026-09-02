"""Python result types mirroring the Java sealed interface hierarchy."""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum


# ── Validation ────────────────────────────────────────────────────────────────

@dataclass(frozen=True)
class ValidationSuccess:
    """validate() succeeded; the input is a valid UCUM expression."""
    term_string: str


@dataclass(frozen=True)
class ValidationFailure:
    """validate() failed; the input is not valid UCUM."""
    messages: list[str]


ValidationResult = ValidationSuccess | ValidationFailure


# ── Canonicalization ──────────────────────────────────────────────────────────

@dataclass(frozen=True)
class CanonicalizationSuccess:
    """canonicalize() succeeded."""
    magnitude: str       # string form of the PreciseDecimal conversion factor
    canonical_term: str  # UCUM syntax string of the canonical term


@dataclass(frozen=True)
class CanonicalizationFailedArbitraryUnit:
    """canonicalize() failed — term contains an arbitrary unit."""
    arbitrary_unit: str


@dataclass(frozen=True)
class CanonicalizationFailedParserError:
    """canonicalize() failed — input could not be parsed."""


@dataclass(frozen=True)
class CanonicalizationFailedPHWithMass:
    """canonicalize() failed — [pH] cannot be canonicalized to mass."""


CanonicalizationResult = (
    CanonicalizationSuccess
    | CanonicalizationFailedArbitraryUnit
    | CanonicalizationFailedParserError
    | CanonicalizationFailedPHWithMass
)


# ── Conversion ────────────────────────────────────────────────────────────────

@dataclass(frozen=True)
class ConversionSuccess:
    """convert() succeeded. conversion_factor x satisfies: factor * from = x * to."""
    conversion_factor: str  # string form of PreciseDecimal


@dataclass(frozen=True)
class ConversionFailedBaseDimensionMismatch:
    """convert() failed — the two terms live in different base dimensions."""
    details: str


@dataclass(frozen=True)
class ConversionFailedCanonicalization:
    """convert() failed — canonicalization of one or both terms failed."""
    details: str


@dataclass(frozen=True)
class ConversionFailedParserError:
    """convert() failed — one or both inputs could not be parsed."""


ConversionResult = (
    ConversionSuccess
    | ConversionFailedBaseDimensionMismatch
    | ConversionFailedCanonicalization
    | ConversionFailedParserError
)


# ── Relation / Commensurability ───────────────────────────────────────────────

@dataclass(frozen=True)
class RelationIsEqual:
    """The two terms are semantically equal."""
    strict_equal: bool           # identical syntax
    equal_after_processing: bool # equal after canonicalization
    common_term: str             # UCUM syntax of the shared canonical form


@dataclass(frozen=True)
class RelationIsCommensurable:
    """The two terms are commensurable (same base dimensions)."""


@dataclass(frozen=True)
class RelationNotCommensurable:
    """The two terms are not commensurable."""
    diff: dict[str, int]  # DimensionType name → exponent difference


@dataclass(frozen=True)
class RelationFailure:
    """Relation check failed (e.g. canonicalization error)."""


@dataclass(frozen=True)
class RelationParserError:
    """Relation check failed — input could not be parsed."""


RelationResult = (
    RelationIsEqual
    | RelationIsCommensurable
    | RelationNotCommensurable
    | RelationFailure
    | RelationParserError
)

CommensurableResult = (
    RelationIsCommensurable
    | RelationNotCommensurable
    | RelationParserError
)


# ── Print ─────────────────────────────────────────────────────────────────────

class PrintType(Enum):
    UCUM_SYNTAX = "UCUM_SYNTAX"
    EXPRESSIVE_UCUM_SYNTAX = "EXPRESSIVE_UCUM_SYNTAX"
    COMMON_MATH_SYNTAX = "COMMON_MATH_SYNTAX"
    LATEX_SYNTAX = "LATEX_SYNTAX"
