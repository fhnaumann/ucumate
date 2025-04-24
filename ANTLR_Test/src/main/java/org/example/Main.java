package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.example.funcs.Converter;
import org.example.model.Canonicalizer;
import org.example.funcs.PrettyPrinter;
import org.example.model.Expression;
import org.example.util.PreciseDecimal;

public class Main {

    public static Expression visitTerm(String input) {
        NewUCUMLexer lexer = new NewUCUMLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NewUCUMParser parser = new NewUCUMParser(tokens);
        ParseTree tree = parser.mainTerm();
        MyUCUMVisitor visitor = new MyUCUMVisitor(UCUMRegistry.getInstance());

        return visitor.visit(tree);
    }

    public static void main(String[] args) {
//        String input = "a=5\nb=6\na+b*2\n";
        //String input = "(1/2).k[ft_i]+3";
//        String input = "Cel";
//        NewUCUMLexer lexer = new NewUCUMLexer(CharStreams.fromString(input));
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        NewUCUMParser parser = new NewUCUMParser(tokens);
//        ParseTree tree = parser.mainTerm();
//        MyUCUMVisitor visitor = new MyUCUMVisitor(UCUMRegistry.getInstance());
//
//        Expression expression = visitor.visit(tree);
//        System.out.println(expression);
//        PrettyPrinter prettyPrinter = new PrettyPrinter(true, true, true);
//        String abc = prettyPrinter.print(expression);
//        System.out.println(abc);
//
//        Canonicalizer canonicalizer = new Canonicalizer();
//        Canonicalizer.CanonicalizationResult result = canonicalizer.canonicalize((Expression.Term) expression, new Canonicalizer.SpecialUnitConversionContext(1, Canonicalizer.SpecialUnitApplicationDirection.NO_SPECIAL_INVOLVED));
//        switch(result) {
//            case Canonicalizer.Success(double conversionFactor, Expression.Term term,
//                                       Canonicalizer.SpecialUnitApplicationDirection direction
//                                       ) -> {
//                System.out.println(conversionFactor);
//                System.out.println(prettyPrinter.print(term));
//            }
//            case Canonicalizer.TermHasArbitraryUnit(UCUMDefinition.ArbitraryUnit arbitraryUnit) -> {
//                System.out.println("Failed because arbitrary unit is in term: " + arbitraryUnit);
//            }
//        }

        PreciseDecimal factor = new PreciseDecimal("5");
        String unit1 = "K";
        String unit2 = "Cel";
        Converter converter = new Converter();
        Converter.ConversionResult conversionResult = converter.convert(new Converter.Conversion(factor, (Expression.Term) visitTerm(unit1)), (Expression.Term) visitTerm(unit2));
        switch(conversionResult) {
            case Converter.FailedConversion failedConversion -> System.out.println(failedConversion);
            case Converter.Success success -> System.out.printf("%s * %s = %s * %s%n", factor, unit1, success.conversionFactor(), unit2);
        }


        //Term.UnitStep unitStep = null;
        //unitStep.unit("gfd").exponent(3).annotation("gfdd").tmp().build();

        /*

        CalculatorLexer lexer = new CalculatorLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CalculatorParser parser = new CalculatorParser(tokens);
        ParseTree tree = parser.calc();
        //System.out.println(tree);
        CalcVisitor evalVisitor = new CalcVisitor();
        evalVisitor.visit(tree);*/


        /*
        Term term1 = componentBuilder.withUnit(...)
            .withExponent()
            .withAnnotation()
            .buildAsTerm()
        Term term2 = componentBuilder.withUnit(...)
            .withAnnotation()
            .build()
        Term all = TermBuilder.start()
            .withCompose(Term left, Operation op, Term right);
        Term all = TermBuilder.start().withComponent(Component comp).build();
         */


        /*
        HelloLexer lexer = new HelloLexer(CharStreams.fromString(input));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        HelloParser parser = new HelloParser(tokens);

        //System.out.println(tree.toStringTree());
        parser.removeErrorListeners();
        parser.addErrorListener(new HelloErrorListener());
        ParseTree tree = parser.r();
        //ParseTreeWalker walker = new ParseTreeWalker();
        //HelloBaseListener listener = new TmpListener();
        //walker.walk(listener, tree);

        JFrame frame = new JFrame("Parse Tree");
        JPanel panel = new JPanel();

        // Create TreeViewer and set options
        TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
        viewer.setScale(1.5); // Scale the tree for better visibility

        panel.add(viewer);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
        */
    }
}