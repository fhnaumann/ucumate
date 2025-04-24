package org.example;


import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

public class CommonUCUMCodesParsingTest {

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {

    }

    @ParameterizedTest()
    @CsvFileSource(resources = "/common_ucum.csv")
    public void test(int row, String ucumCode, String description) {

        Assertions.assertDoesNotThrow(() -> {
            NewUCUMLexer lexer = new NewUCUMLexer(CharStreams.fromString(ucumCode));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NewUCUMParser parser = new NewUCUMParser(tokens);
            parser.removeErrorListeners(); // Remove default console errors
            parser.addErrorListener(new BaseErrorListener() { // Capture errors
                @Override
                public void syntaxError(
                        Recognizer<?, ?> recognizer, Object offendingSymbol,
                        int line, int charPositionInLine,
                        String msg, RecognitionException e) {
                    throw new RuntimeException(ucumCode + " Syntax error at line " + line + ":" + charPositionInLine + " " + msg);
                }
            });
            ParseTree tree = parser.mainTerm();
        });
    }
}
