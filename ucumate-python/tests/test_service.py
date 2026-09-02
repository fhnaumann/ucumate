"""Integration tests for the Python ucumate bindings.

Run with:
    cd ucumate-python
    pip install -e ".[dev]"
    pytest tests/
"""

import pytest
from ucumate import (
    UCUMService,
    CanonicalizationSuccess,
    CanonicalizationFailedParserError,
    ConversionSuccess,
    ConversionFailedBaseDimensionMismatch,
    ConversionFailedParserError,
    PrintType,
    RelationIsCommensurable,
    RelationIsEqual,
    RelationNotCommensurable,
    RelationParserError,
    ValidationFailure,
    ValidationSuccess,
)


@pytest.fixture(scope="session")
def svc() -> UCUMService:
    return UCUMService()


# ── Validate ──────────────────────────────────────────────────────────────────

class TestValidate:
    def test_valid_unit(self, svc):
        result = svc.validate("mg/dL")
        assert isinstance(result, ValidationSuccess)

    def test_invalid_unit(self, svc):
        result = svc.validate("not_a_unit!!!")
        assert isinstance(result, ValidationFailure)
        assert result.messages  # at least one error message

    def test_validate_to_bool_true(self, svc):
        assert svc.validate_to_bool("kg")

    def test_validate_to_bool_false(self, svc):
        assert not svc.validate_to_bool("???")

    def test_dimensionless_unity(self, svc):
        result = svc.validate("1")
        assert isinstance(result, ValidationSuccess)


# ── Canonicalize ──────────────────────────────────────────────────────────────

class TestCanonicalize:
    def test_simple(self, svc):
        result = svc.canonicalize("m/s2")
        assert isinstance(result, CanonicalizationSuccess)
        assert "m" in result.canonical_term
        assert result.magnitude == "1"

    def test_with_factor(self, svc):
        result = svc.canonicalize("kg", factor="1000")
        assert isinstance(result, CanonicalizationSuccess)

    def test_invalid_input(self, svc):
        result = svc.canonicalize("!!!invalid!!!")
        assert isinstance(result, CanonicalizationFailedParserError)

    def test_cancellation(self, svc):
        # m/(s.m) should simplify to s-1
        result = svc.canonicalize("m/(s.m)")
        assert isinstance(result, CanonicalizationSuccess)
        assert "s" in result.canonical_term


# ── Convert ───────────────────────────────────────────────────────────────────

class TestConvert:
    def test_same_dimension(self, svc):
        result = svc.convert("g", "kg")
        assert isinstance(result, ConversionSuccess)
        assert result.conversion_factor == "0.001"

    def test_with_factor(self, svc):
        result = svc.convert("g", "kg", factor="500")
        assert isinstance(result, ConversionSuccess)
        assert result.conversion_factor == "0.5"

    def test_dimension_mismatch(self, svc):
        result = svc.convert("m", "s")
        assert isinstance(result, ConversionFailedBaseDimensionMismatch)

    def test_invalid_from(self, svc):
        result = svc.convert("!!!!", "kg")
        assert isinstance(result, ConversionFailedParserError)

    def test_invalid_to(self, svc):
        result = svc.convert("g", "!!!!")
        assert isinstance(result, ConversionFailedParserError)

    def test_temperature(self, svc):
        result = svc.convert("Cel", "K")
        assert isinstance(result, ConversionSuccess)

    def test_km_to_m(self, svc):
        result = svc.convert("km", "m")
        assert isinstance(result, ConversionSuccess)
        assert result.conversion_factor == "1000"


# ── Relation / Commensurability ───────────────────────────────────────────────

class TestRelation:
    def test_equal_terms(self, svc):
        result = svc.check_relation("m", "m")
        assert isinstance(result, RelationIsEqual)
        assert result.strict_equal

    def test_commensurable(self, svc):
        result = svc.check_relation("m", "km")
        assert isinstance(result, RelationIsCommensurable)

    def test_not_commensurable(self, svc):
        result = svc.check_commensurable("m", "s")
        assert isinstance(result, RelationNotCommensurable)
        assert result.diff  # non-empty diff map

    def test_commensurable_check(self, svc):
        result = svc.check_commensurable("g", "kg")
        assert isinstance(result, RelationIsCommensurable)

    def test_parser_error(self, svc):
        result = svc.check_commensurable("!!!!", "kg")
        assert isinstance(result, RelationParserError)


# ── Print ─────────────────────────────────────────────────────────────────────

class TestPrint:
    def test_ucum_syntax(self, svc):
        rendered = svc.print("m/s2")
        assert isinstance(rendered, str)
        assert rendered  # non-empty

    def test_latex(self, svc):
        rendered = svc.print("m/s2", PrintType.LATEX_SYNTAX)
        assert isinstance(rendered, str)

    def test_common_math(self, svc):
        rendered = svc.print("kg.m/s2", PrintType.COMMON_MATH_SYNTAX)
        assert isinstance(rendered, str)
