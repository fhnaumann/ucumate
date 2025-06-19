package io.github.fhnaumann;

import io.github.fhnaumann.funcs.UCUMService;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.printer.Printer;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.UCUMRegistry;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Felix Naumann
 */
public class Main {
    public static void main(String[] args) {
        visit("cft_i");
        //System.out.println(UCUMService.print(parsed, Printer.PrintType.EXPRESSIVE_UCUM_SYNTAX));
    }

    public sealed interface Result {}
    public record Success(UCUMExpression.Term term) implements Result {}
    public record Failure(List<String> errorMessages) implements Result {}

    public static Result visit(String input) {
        ErrorFeedbackUCUMLexer lexer = new ErrorFeedbackUCUMLexer(CharStreams.fromString(input));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new Validator.LexerException("Lexical error at line " + line + ":" + charPositionInLine + ": " + msg);
            }
        });
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        List<String> errorMessages = new ArrayList<>();
        SyntaxMatchHelper.checkWhiteSpace(tokens, errorMessages);

        // small hack to continue parsing if an error with spacing has occurred


        ErrorFeedbackUCUMParser parser = new ErrorFeedbackUCUMParser(tokens);
        // parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new Validator.ParserException("Syntax error at line " + line + ":" + charPositionInLine + ": " + msg);
            }
        });
        ParseTree tree = parser.mainTerm();
        MyFeedbackVisitor visitor = new MyFeedbackVisitor(UCUMRegistry.getInstance(), errorMessages);
        try {
            UCUMExpression.Term term = (UCUMExpression.Term) visitor.visit(tree);
            // not all errors throw an actual exceptions, some just add an error message
            if(errorMessages.isEmpty()) {
                return new Success(term);
            }
            else {
                return new Failure(errorMessages);
            }
        } catch (Validator.ParserException e) {
            e.printStackTrace();
            errorMessages.addAll(SyntaxMatchHelper.extractErrorMessagesFrom(e));
            return new Failure(errorMessages);
        } finally {
            // System.out.println(visitor.getErrorMessages());
        }
    }
}