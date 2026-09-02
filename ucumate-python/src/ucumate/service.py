"""Python wrapper around the Java UCUMService."""

from __future__ import annotations

from typing import Optional

from ._jvm import ensure_jvm
from ._results import (
    CanonicalizationFailedArbitraryUnit,
    CanonicalizationFailedParserError,
    CanonicalizationFailedPHWithMass,
    CanonicalizationResult,
    CanonicalizationSuccess,
    CommensurableResult,
    ConversionFailedBaseDimensionMismatch,
    ConversionFailedCanonicalization,
    ConversionFailedParserError,
    ConversionResult,
    ConversionSuccess,
    PrintType,
    RelationFailure,
    RelationIsCommensurable,
    RelationIsEqual,
    RelationNotCommensurable,
    RelationParserError,
    RelationResult,
    ValidationFailure,
    ValidationResult,
    ValidationSuccess,
)


class UCUMService:
    """
    Python mirror of the Java UCUMService from ucumate-core.

    Wraps validation, canonicalization, conversion, relation checking,
    and printing of UCUM expressions.  All methods accept plain Python
    strings and return typed Python result objects.

    Parameters
    ----------
    ucum_version:
        Optional UCUM version string (e.g. "2.1").  Defaults to the
        version configured in ucumate's ConfigurationRegistry.
    """

    def __init__(self, ucum_version: Optional[str] = None) -> None:
        ensure_jvm()
        import jpype

        JUCUMService = jpype.JClass("io.github.fhnaumann.funcs.UCUMService")
        self._svc = JUCUMService(ucum_version) if ucum_version is not None else JUCUMService()

        # Cache all Java result-type class references (use JVM binary names for nested types).
        self._JValidatorSuccess = jpype.JClass("io.github.fhnaumann.funcs.ValidatorService$Success")
        self._JParserError = jpype.JClass("io.github.fhnaumann.funcs.ValidatorService$ParserError")
        self._JCanonSuccess = jpype.JClass("io.github.fhnaumann.funcs.CanonicalizerService$Success")
        self._JCanonArbitrary = jpype.JClass("io.github.fhnaumann.funcs.CanonicalizerService$TermHasArbitraryUnit")
        self._JCanonPH = jpype.JClass("io.github.fhnaumann.funcs.CanonicalizerService$TermContainsPHAndCanonicalizingToMass")
        self._JConvSuccess = jpype.JClass("io.github.fhnaumann.funcs.ConverterService$Success")
        self._JConvDimMismatch = jpype.JClass("io.github.fhnaumann.funcs.ConverterService$BaseDimensionMismatch")
        self._JConvCanonFail = jpype.JClass("io.github.fhnaumann.funcs.ConverterService$FailedCanonicalization")
        self._JRelIsEqual = jpype.JClass("io.github.fhnaumann.funcs.RelationCheckerService$IsEqual")
        self._JRelIsComm = jpype.JClass("io.github.fhnaumann.funcs.RelationCheckerService$IsCommensurable")
        self._JRelNotComm = jpype.JClass("io.github.fhnaumann.funcs.RelationCheckerService$NotCommensurable")
        self._JRelFailure = jpype.JClass("io.github.fhnaumann.funcs.RelationCheckerService$Failure")
        self._JPreciseDecimal = jpype.JClass("io.github.fhnaumann.util.PreciseDecimal")
        self._JPrintType = jpype.JClass("io.github.fhnaumann.funcs.printer.Printer$PrintType")

    # ── Validate ──────────────────────────────────────────────────────────────

    def validate(self, term: str) -> ValidationResult:
        """
        Validate a UCUM expression string.

        Returns
        -------
        ValidationSuccess
            When *term* is a valid UCUM expression.  ``term_string`` holds
            the canonical UCUM syntax of the parsed term.
        ValidationFailure
            When *term* is invalid.  ``messages`` lists the parser errors.
        """
        result = self._svc.validate(term)
        if isinstance(result, self._JValidatorSuccess):
            return ValidationSuccess(term_string=str(self._svc.print_(result.term())))
        # Failure record: errorMessages() returns List<String>
        msgs = [str(m) for m in result.errorMessages()]
        return ValidationFailure(messages=msgs)

    def validate_to_bool(self, term: str) -> bool:
        """Return True iff *term* is a valid UCUM expression."""
        return isinstance(self.validate(term), ValidationSuccess)

    # ── Canonicalize ──────────────────────────────────────────────────────────

    def canonicalize(
        self,
        term: str,
        factor: str = "1",
        substance_molar_mass: Optional[str] = None,
    ) -> CanonicalizationResult:
        """
        Canonicalize a UCUM expression.

        The canonical form uses only multiplication and integer exponents,
        e.g. ``m/s2`` → ``m.s-2``.

        Parameters
        ----------
        term:
            UCUM expression string.
        factor:
            Numeric multiplier applied before canonicalization (default ``"1"``).
        substance_molar_mass:
            Molar mass coefficient for mol↔mass conversions (optional).
        """
        if substance_molar_mass is None:
            # Use the string overload — it handles parser errors internally.
            result = self._svc.canonicalize(factor, term)
        else:
            try:
                j_term = self._svc.parseOrError(term)
            except Exception:
                return CanonicalizationFailedParserError()
            result = self._svc.canonicalize(
                self._JPreciseDecimal(factor),
                j_term,
                self._JPreciseDecimal(substance_molar_mass),
            )
        return self._map_canon(result)

    def _map_canon(self, result) -> CanonicalizationResult:
        if isinstance(result, self._JCanonSuccess):
            return CanonicalizationSuccess(
                magnitude=str(result.magnitude()),
                canonical_term=str(self._svc.print_(result.canonicalTerm())),
            )
        if isinstance(result, self._JCanonArbitrary):
            return CanonicalizationFailedArbitraryUnit(
                arbitrary_unit=str(result.arbitraryUnit().code())
            )
        if isinstance(result, self._JCanonPH):
            return CanonicalizationFailedPHWithMass()
        return CanonicalizationFailedParserError()

    # ── Convert ───────────────────────────────────────────────────────────────

    def convert(
        self,
        from_term: str,
        to_term: str,
        factor: str = "1",
        substance_molar_mass: Optional[str] = None,
    ) -> ConversionResult:
        """
        Convert between two UCUM expressions.

        Solves ``factor * from_term = x * to_term`` and returns ``x``.

        Parameters
        ----------
        from_term:
            Source UCUM expression.
        to_term:
            Target UCUM expression.
        factor:
            Numeric multiplier on the source (default ``"1"``).
        substance_molar_mass:
            Molar mass coefficient for mol↔mass conversions (optional).
        """
        if substance_molar_mass is not None:
            result = self._svc.convert(factor, from_term, to_term, substance_molar_mass)
        else:
            result = self._svc.convert(factor, from_term, to_term)
        return self._map_conv(result)

    def _map_conv(self, result) -> ConversionResult:
        if isinstance(result, self._JConvSuccess):
            return ConversionSuccess(conversion_factor=str(result.conversionFactor()))
        if isinstance(result, self._JConvDimMismatch):
            return ConversionFailedBaseDimensionMismatch(details=str(result.failure()))
        if isinstance(result, self._JConvCanonFail):
            return ConversionFailedCanonicalization(
                details=str(result.failedCanonicalization())
            )
        return ConversionFailedParserError()

    # ── Relation ──────────────────────────────────────────────────────────────

    def check_relation(self, term1: str, term2: str) -> RelationResult:
        """
        Check the relation between two UCUM terms.

        Returns one of:
        - ``RelationIsEqual`` — the terms are semantically equal.
        - ``RelationIsCommensurable`` — same base dimensions, not equal.
        - ``RelationNotCommensurable`` — different base dimensions.
        - ``RelationFailure`` / ``RelationParserError`` on error.
        """
        try:
            j1 = self._svc.parseOrError(term1)
            j2 = self._svc.parseOrError(term2)
        except Exception:
            return RelationParserError()
        result = self._svc.checkRelation(j1, j2)
        return self._map_relation(result)

    def check_commensurable(self, term1: str, term2: str) -> CommensurableResult:
        """
        Check whether two UCUM terms are commensurable (share the same base dimensions).

        Returns ``RelationIsCommensurable``, ``RelationNotCommensurable``,
        or ``RelationParserError``.
        """
        result = self._svc.checkCommensurable(term1, term2)
        return self._map_commensurable(result)

    def _map_relation(self, result) -> RelationResult:
        if isinstance(result, self._JRelIsEqual):
            return RelationIsEqual(
                strict_equal=bool(result.strictEqual()),
                equal_after_processing=bool(result.equalAfterProcessing()),
                common_term=str(self._svc.print_(result.termThatIsEqual())),
            )
        if isinstance(result, self._JRelIsComm):
            return RelationIsCommensurable()
        if isinstance(result, self._JRelNotComm):
            return RelationNotCommensurable(
                diff={str(e.getKey()): int(e.getValue()) for e in result.diff().entrySet()}
            )
        if isinstance(result, self._JParserError):
            return RelationParserError()
        return RelationFailure()

    def _map_commensurable(self, result) -> CommensurableResult:
        if isinstance(result, self._JRelIsComm):
            return RelationIsCommensurable()
        if isinstance(result, self._JRelNotComm):
            return RelationNotCommensurable(
                diff={str(e.getKey()): int(e.getValue()) for e in result.diff().entrySet()}
            )
        return RelationParserError()

    # ── Print ─────────────────────────────────────────────────────────────────

    def print(self, term: str, print_type: PrintType = PrintType.UCUM_SYNTAX) -> str:
        """
        Render a UCUM expression as a string.

        Parameters
        ----------
        term:
            UCUM expression string to render.
        print_type:
            Output format.  Defaults to ``PrintType.UCUM_SYNTAX``.

        Raises
        ------
        Exception
            If *term* cannot be parsed.
        """
        j_type = getattr(self._JPrintType, print_type.value)
        return str(self._svc.print_(term, j_type))
