package io.github.fhnaumann.funcs;

import io.github.fhnaumann.NewUCUMBaseVisitor;
import io.github.fhnaumann.NewUCUMLexer;
import io.github.fhnaumann.NewUCUMParser;
import io.github.fhnaumann.configuration.ConfigurationRegistry;
import io.github.fhnaumann.model.CanonicalUCUMSyntaxVisitor;
import io.github.fhnaumann.model.UCUMSyntaxVisitor;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UCUMExpression.Term;
import io.github.fhnaumann.util.ParseUtil;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    public sealed interface ValidationResult {}

    public record Success(UCUMExpression.Term term) implements ValidationResult {}
    public record Failure(String message) implements ValidationResult {
        public Failure() {
            this("");
        }
    }

    public static class LexerException extends RuntimeException {
        public LexerException(String message) {
            super(message);
        }
    }
    public static class ParserException extends RuntimeException {

        private final List<ParseUtil.FailureResult> failures;

        public ParserException(String message) {
            super(message);
            this.failures = new ArrayList<>();
        }
        public ParserException(ParseUtil.FailureResult failureResult) {
            super(failureResult.failedText());
            this.failures = List.of(failureResult);
        }
        public ParserException(ParseUtil.InvalidResults invalidResults) {
            super(invalidResults.toString());
            this.failures = invalidResults.failureResults();
        }

        public List<ParseUtil.FailureResult> getFailures() {
            return failures;
        }
    }

    public record ParserError() implements
            Canonicalizer.FailedCanonicalization,
            Converter.FailedConversion, RelationChecker.FailedRelationCheck,
            RelationChecker.FailedCommensurableCheck
    {}

    /**
     * Internal use only!
     * @param input The string input that is definitely in a canonical form
     * @return The parsed canonical term, avoiding expensive calls to {@link Canonicalizer}.
     */
    public static UCUMExpression.CanonicalTerm parseCanonical(String input) {
        return (UCUMExpression.CanonicalTerm) validateImpl(input, new CanonicalUCUMSyntaxVisitor(UCUMRegistry.getInstance()));
    }

    /**
     * Internal use only!
     * @param input The string input that
     * @return The validation result. Avoids infinite recursion by bypassing registry here.
     */
    public static Term parseByPassChecks(String input) {
        return validateImpl(input, new UCUMSyntaxVisitor(UCUMRegistry.getInstance()));
    }

    public static ValidationResult validate(String input) {
        ValidationResult cached = PersistenceRegistry.getInstance().getValidated(input);
        if(cached != null) {
            /*
            This is redundant for the cache itself, but there is a specific scenario where this is needed:
            When the cache and preHeat are enabled and the user inputs a unit that is in the pre-heated list, then
            it will have cache hit and return here, but the unit is not saved in any of the additional providers.
             */
            if(PersistenceRegistry.hasAny()) {
                PersistenceRegistry.getInstance().saveValidated(input, cached);
            }
            return cached;
        }

        /*
        Optional<UCUMUnit> optionalUCUMUnit = UCUMRegistry.getInstance().getUCUMUnit(input);
        if(optionalUCUMUnit.isPresent()) {
            return new Success(SoloTermBuilder.builder().withoutPrefix(optionalUCUMUnit.get()).noExpNoAnnot().asTerm().build());
        }
        */
        try {
            ValidationResult result;
            Term term = validateImpl(input, new UCUMSyntaxVisitor(UCUMRegistry.getInstance()));
            if(!ConfigurationRegistry.get().isAllowAnnotAfterParens() && hasAnnotationAfterParens(term)) {
                log.warn("Encountered term {} with an annotation on parenthesis but the property {} is disabled.", input, "ucumate.allowAnnotAfterParens");
                result = new Failure();
                PersistenceRegistry.getInstance().saveValidated(input, result);
                return result;
            }
            SpecialChecker.SpecialCheckResult specialCheckResult = SpecialChecker.checkForSpecialUnitInTerm(term, new SpecialChecker.SpecialCheckResult(false, false,false));
            result = specialCheckResult.isValid() ? new Success(term) : new Failure();
            PersistenceRegistry.getInstance().saveValidated(input, result);
            return result;
        } catch (LexerException | ParserException e) {
            ValidationResult result = new Failure();
            PersistenceRegistry.getInstance().saveValidated(input, result);
            return result;
        }
    }

    private static Term validateImpl(String input, NewUCUMBaseVisitor<UCUMExpression> visitor) {
        NewUCUMLexer lexer = new NewUCUMLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new LexerException("Lexical error at line " + line + ":" + charPositionInLine + ": " + msg);
            }
        });
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewUCUMParser parser = new NewUCUMParser(tokens);
        // parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new ParserException("Syntax error at line " + line + ":" + charPositionInLine + ": " + msg);
            }
        });
        ParseTree tree = parser.mainTerm();
        UCUMExpression.Term term = (UCUMExpression.Term) visitor.visit(tree);
        return term;
    }

    private static boolean hasAnnotationAfterParens(Term term) {
        return switch (term) {
            case UCUMExpression.ComponentTerm componentTerm -> false;
            case UCUMExpression.AnnotOnlyTerm annotOnlyTerm -> false;
            case UCUMExpression.BinaryTerm binaryTerm -> hasAnnotationAfterParens(binaryTerm.left()) || hasAnnotationAfterParens(binaryTerm.right());
            case UCUMExpression.UnaryDivTerm unaryDivTerm -> hasAnnotationAfterParens(unaryDivTerm.term());
            case UCUMExpression.AnnotTerm annotTerm -> annotTerm.term() instanceof UCUMExpression.ParenTerm;
            case UCUMExpression.ParenTerm parenTerm -> hasAnnotationAfterParens(parenTerm.term());
        };
    }
}
