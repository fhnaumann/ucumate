package org.example.funcs;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.example.MyUCUMVisitor;
import org.example.NewUCUMLexer;
import org.example.NewUCUMParser;
import org.example.UCUMRegistry;
import org.example.model.Expression;
import org.example.util.ParseUtil;

public class Validator {

    public sealed interface ValidationResult {}

    public record Success(Expression.Term term) implements ValidationResult {}
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
        try {
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
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                    throw new ParserException("Syntax error at line " + line + ":" + charPositionInLine + ": " + msg);
                }
            });
            ParseTree tree = parser.mainTerm();
            MyUCUMVisitor visitor = new MyUCUMVisitor(UCUMRegistry.getInstance());
            Expression.Term term = (Expression.Term) visitor.visit(tree);
            return new Success(term);
        } catch (LexerException | ParserException e) {
            return new Failure();
        }
    }
}
