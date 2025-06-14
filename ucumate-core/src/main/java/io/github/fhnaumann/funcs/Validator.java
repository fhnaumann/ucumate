package io.github.fhnaumann.funcs;

import io.github.fhnaumann.NewUCUMBaseVisitor;
import io.github.fhnaumann.NewUCUMLexer;
import io.github.fhnaumann.NewUCUMParser;
import io.github.fhnaumann.model.CanonicalUCUMSyntaxVisitor;
import io.github.fhnaumann.model.UCUMSyntaxVisitor;
import io.github.fhnaumann.persistence.PersistenceRegistry;
import io.github.fhnaumann.util.UCUMRegistry;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.model.UCUMExpression.Term;
import io.github.fhnaumann.util.ParseUtil;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

public class Validator {

    public sealed interface ValidationResult {}

    public record Success(UCUMExpression.Term term) implements ValidationResult {}
    public record Failure() implements ValidationResult {}

    public static class LexerException extends RuntimeException {
        public LexerException(String message) {
            super(message);
        }
    }
    public static class ParserException extends RuntimeException {
        public ParserException(String message) {
            super(message);
        }
        public ParserException(ParseUtil.FailureResult failureResult) {}
        public ParserException(ParseUtil.InvalidResults invalidResults) {}
    }

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
            return cached;
        }

        /*
        Optional<UCUMUnit> optionalUCUMUnit = UCUMRegistry.getInstance().getUCUMUnit(input);
        if(optionalUCUMUnit.isPresent()) {
            return new Success(SoloTermBuilder.builder().withoutPrefix(optionalUCUMUnit.get()).noExpNoAnnot().asTerm().build());
        }
        */
        try {
            Term term = validateImpl(input, new UCUMSyntaxVisitor(UCUMRegistry.getInstance()));
            SpecialChecker.SpecialCheckResult specialCheckResult = SpecialChecker.checkForSpecialUnitInTerm(term, new SpecialChecker.SpecialCheckResult(false, false,false));
            ValidationResult result = specialCheckResult.isValid() ? new Success(term) : new Failure();
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
}
