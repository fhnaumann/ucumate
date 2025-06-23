package io.github.fhnaumann;

import io.github.fhnaumann.funcs.SpecialChecker;
import io.github.fhnaumann.funcs.Validator;
import io.github.fhnaumann.funcs.ValidatorService;
import io.github.fhnaumann.model.UCUMExpression;
import io.github.fhnaumann.util.UCUMRegistry;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Felix Naumann
 */
public class FeedbackValidator implements ValidatorService {

    private static final ValidatorService normalValidator = new Validator();

    @Override
    public ValidationResult validate(String input) {
        return switch (normalValidator.validate(input)) {
            case Success success -> success;
            case Failure failure -> validateImpl(input);
        };
    }

    private ValidationResult validateImpl(String input) {
        List<String> errorMessages = new ArrayList<>();
        try {
            ErrorFeedbackUCUMLexer lexer = new ErrorFeedbackUCUMLexer(CharStreams.fromString(input));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                    throw new Validator.LexerException("Lexical error at line " + line + ":" + charPositionInLine + ": " + msg);
                }
            });
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            SyntaxMatchHelper.checkWhiteSpace(tokens, errorMessages);

            ErrorFeedbackUCUMParser parser = new ErrorFeedbackUCUMParser(tokens);
            // parser.getInterpreter().setPredictionMode(PredictionMode.LL);
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                    throw new Validator.ParserException("Syntax error at line " + line + ":" + charPositionInLine + ": " + msg);
                }
            });
            ParserRuleContext tree = parser.mainTerm();

            SyntaxMatchHelper.searchForAnyUnbalancedParens(tokens, tree, errorMessages);

            MyFeedbackVisitor visitor = new MyFeedbackVisitor(UCUMRegistry.getInstance(), new ArrayList<>());
            UCUMExpression.Term term = (UCUMExpression.Term) visitor.visit(tree);

            if(term != null) {
                SpecialChecker.SpecialCheckResult specialCheckResult = SpecialChecker.checkForSpecialUnitInTerm(term, new SpecialChecker.SpecialCheckResult(false, false,false));
                if(!specialCheckResult.isValid() && specialCheckResult.containsSpecialUnit()) {
                    if(specialCheckResult.containsDivision()) {
                        errorMessages.add(ErrorMessages.get("special_arithmetic_with_division"));
                    }
                    if(specialCheckResult.containsExponent()) {
                        errorMessages.add(ErrorMessages.get("special_arithmetic_with_exponent"));
                    }
                }
            }


            errorMessages.addAll(visitor.getErrorMessages());
            // not all errors throw an actual exceptions, some just add an error message
            if(errorMessages.isEmpty()) {
                return new Success(term);
            }
            else {
                return new Failure(errorMessages);
            }
        } catch (Validator.ParserException | LexerException e) {
            //e.printStackTrace();

            String analysedMessage = SyntaxMatchHelper.analyseUnitForErrorDetails(input);
            if(analysedMessage != null) {
                errorMessages.add(analysedMessage);
            }
            else {
                if(e instanceof ParserException parserException) {
                    errorMessages.addAll(SyntaxMatchHelper.extractErrorMessagesFrom(parserException));
                }
                else if(e instanceof LexerException lexerException) {
                    errorMessages.addAll(SyntaxMatchHelper.extractErrorMessagesFrom(lexerException));

                }
            }
            return new Failure(errorMessages);
        }
    }
}
