package me.fhnau.org.funcs;

import me.fhnau.org.MyUCUMVisitor;
import me.fhnau.org.MyUCUMVisitor.SpecialCheckResult;
import me.fhnau.org.UCUMDefinition.UCUMUnit;
import me.fhnau.org.UCUMRegistry;
import me.fhnau.org.builders.SoloTermBuilder;
import me.fhnau.org.model.UCUMExpression;
import me.fhnau.org.model.UCUMExpression.Term;
import me.fhnau.org.util.ParseUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.example.NewUCUMLexer;
import org.example.NewUCUMParser;

import java.util.Optional;

public class Validator {

    public static final Cache<String, ValidationResult> cache = Caffeine.newBuilder().maximumSize(10_000).recordStats().build();

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

    public static ValidationResult validate(String input) {
        ValidationResult cached = cache.getIfPresent(input);
        if(cached != null) {
            return cached;
        }

        Optional<UCUMUnit> optionalUCUMUnit = UCUMRegistry.getInstance().getUCUMUnit(input);
        if(optionalUCUMUnit.isPresent()) {
            return new Success(SoloTermBuilder.builder().withoutPrefix(optionalUCUMUnit.get()).noExpNoAnnot().asTerm().build());
        }
        try {
            Term term = validateImpl(input);
            SpecialCheckResult specialCheckResult = MyUCUMVisitor.checkForSpecialUnitInTerm(term, new SpecialCheckResult(false, false,false));
            ValidationResult result = specialCheckResult.isValid() ? new Success(term) : new Failure();
            cache.put(input, result);
            return result;
        } catch (LexerException | ParserException e) {
            ValidationResult result = new Failure();
            cache.put(input, result);
            return result;
        }
    }

    private static Term validateImpl(String input) {
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
        MyUCUMVisitor visitor = new MyUCUMVisitor(UCUMRegistry.getInstance());
        UCUMExpression.Term term = (UCUMExpression.Term) visitor.visit(tree);
        return term;
    }
}
