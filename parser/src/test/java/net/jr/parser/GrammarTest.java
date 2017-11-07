package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Lexeme;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.impl.Literal;
import net.jr.lexer.impl.SingleChar;
import net.jr.lexer.impl.Word;
import net.jr.parser.ast.AstNode;
import net.jr.parser.impl.LRParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;
import java.util.Stack;

public class GrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    Symbol S = new Forward("S");
    Symbol N = new Forward("N");
    Symbol E = new Forward("E");
    Symbol V = new Forward("V");

    Symbol B = new Forward("V");

    SingleChar x = new SingleChar('x');
    SingleChar eq = new SingleChar('=');

    SingleChar plus = new SingleChar('+');
    SingleChar minus = new SingleChar('-');
    SingleChar mult = new SingleChar('*');
    SingleChar div = new SingleChar('/');

    SingleChar zero = new SingleChar('0');
    SingleChar one = new SingleChar('1');

    Grammar grammar;

    @Before
    public void setup() {

        grammar = new Grammar();

        //1. S → N
        grammar.addRule(S, N);
        //2. N → V = E
        grammar.addRule(N, V, eq, E);
        //3. N → E
        grammar.addRule(N, E);
        //4. E → V
        grammar.addRule(E, V);
        //5. V → x
        grammar.addRule(V, x);
        //6. V → * E
        grammar.addRule(V, mult, E);
    }

    @Test
    public void testParse() {
        Parser parser = grammar.createParser(grammar.getTargetSymbol());
        Lexer lexer = Lexer.forLexemes(grammar.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader("x = *x")));

    }

    @Test
    public void testParse2() {

        Grammar g = new Grammar();

        // (0)
        g.addRule(S, E);

        // (1) E → E * B
        g.addRule(E, E, mult, B);

        // (2) E → E + B
        g.addRule(E, E, plus, B);

        // (3) E → B
        g.addRule(E, B);

        // (4) B → 0
        g.addRule(B, zero);

        // (5) B → 1
        g.addRule(B, one);

        Parser parser = g.createParser(S);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());

        ((LRParser)parser).parse("1+1");
    }

    @Test
    public void testParseSimpleChoice() {
        Symbol V = new Forward("V");
        Grammar g = new Grammar();
        g.addRule(V, new SingleChar('0'));
        g.addRule(V, new SingleChar('1'));

        Parser parser = g.createParser(V);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());

        parser.parse(lexer.iterator(new StringReader("0")));
        parser.parse(lexer.iterator(new StringReader("1")));
    }

    @Test
    public void testParseSimpleList() {
        Grammar g = new Grammar();
        Symbol L = new Forward("list");
        Symbol I = new Forward("inList");
        Symbol ident = Lexemes.cIdentifier();

        g.addRule(L, new SingleChar('('), I, new SingleChar(')'));
        g.addRule(I, ident);
        g.addRule(I, I, new SingleChar(','), ident);
        g.addEmptyRule(I);

        Parser parser = g.createParser();
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        //empty list is ok
        parser.parse(lexer.iterator(new StringReader("()")));

        //list of identifiers
        parser.parse(lexer.iterator(new StringReader("(list, of, identifiers)")));


    }

    @Test
    public void testList() {
        Grammar g = new Grammar();
        Symbol L = new Forward("list");
        g.addRule(L, new SingleChar('('), g.list(new SingleChar(','), Lexemes.cIdentifier()), new SingleChar(')'));

        Parser parser = g.createParser(L);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        //empty list are ok
        parser.parse(lexer.iterator(new StringReader("()")));

        //list of identifiers
        parser.parse(lexer.iterator(new StringReader("(list, of, identifiers)")));

        try {
            parser.parse(lexer.iterator(new StringReader("(list")));
            Assert.fail();
        } catch (ParseError e) {
            //ok!
        }
    }

    @Test
    public void testZeroOrMore() {

        Grammar g = new Grammar();
        Symbol L = new Forward("onomatopoeias");
        g.addRule(L, new Literal(">>"), g.zeroOrMore(Lexemes.cIdentifier(), new SingleChar('!')));

        Parser parser = g.createParser(L);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader(">>")));

        //list of identifiers
        parser.parse(lexer.iterator(new StringReader(">>oh! eh! yeah!")));

    }

    @Test
    public void testOneOrMore() {
        Grammar g = new Grammar();
        Symbol S = new Forward("S");
        g.addRule(S, g.oneOrMore(new Word("nahnah")), new Word("batman"));

        Parser parser = g.createParser(S);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader("nahnah nahnah nahnah nahnah nahnah batman")));
        parser.parse(lexer.iterator(new StringReader("nahnah batman")));

        try {
            parser.parse(lexer.iterator(new StringReader("batman")));
            Assert.fail();
        } catch (ParseError pe) {
            //ok!
        }
    }

    @Test
    public void testFourOps() {
        Assert.assertEquals(8, computeNumber("15-7"));
        Assert.assertEquals(18, computeNumber("3*6"));
        Assert.assertEquals(5, computeNumber("50/10"));
        Assert.assertEquals(32, computeNumber("3 * 6 + 2 * 7"));
        Assert.assertEquals(4, computeNumber("1-2+3-4+5-6+7"));
    }


    private int computeNumber(String expression) {

        Stack<Integer> calculatorStack = new Stack<>();
        Grammar g = new Grammar();
        Symbol E = new Forward("E");

        g.setPrecedenceLevel(20, mult, div);
        g.setPrecedenceLevel(10, plus, minus);

        g.addRule(E, Lexemes.cInteger()).withAction(node -> {
            int value = Integer.parseInt(node.asToken().getMatchedText());
            calculatorStack.push(value);
        });

        g.addRule(E, E, g.oneOf(plus, minus), E)
                .withAssociativity(Associativity.Left)
                .withAction(ctx -> {
                    int topOfStack = calculatorStack.pop();
                    int nextInStack = calculatorStack.pop();
                    String operation = ctx.getChildren().get(1).asToken().getMatchedText();
                    switch (operation) {
                        case "-":
                            calculatorStack.push(nextInStack - topOfStack);
                            break;
                        case "+":
                            calculatorStack.push(nextInStack + topOfStack);
                            break;
                    }
                });

        g.addRule(E, E, g.oneOf(mult, div), E)
                .withAssociativity(Associativity.Left)
                .withAction(ctx -> {
                    int topOfStack = calculatorStack.pop();
                    int nextInStack = calculatorStack.pop();
                    String operation = ctx.getChildren().get(1).asToken().getMatchedText();
                    switch (operation) {
                        case "*":
                            calculatorStack.push(nextInStack * topOfStack);
                            break;
                        case "/":
                            calculatorStack.push(nextInStack / topOfStack);
                            break;
                    }
                });

        Parser parser = g.createParser(E);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader(expression)));

        return calculatorStack.pop();
    }

    @Test
    public void testStability() {
        for(int i=0; i<1000; i++) {
            System.out.println(i);
            testIfElse();
        }
    }

    @Test
    public void testIfElse() {
        Lexeme If = Lexemes.literal("if");
        Lexeme Else = Lexemes.literal("else");
        Lexeme LeftBrace = Lexemes.singleChar('(');
        Lexeme RightBrace = Lexemes.singleChar(')');
        Lexeme Expression = Lexemes.singleChar('E');

        Symbol All = new Forward("All");
        Symbol Statement = new Forward("Statement");
        Symbol SelectionStatement = new Forward("SelectionStatement");

        Grammar g = new Grammar();
        g.addRule(All, g.oneOrMore(Statement));
        g.addRule(Statement, SelectionStatement);
        g.addRule(Statement, Lexemes.literal("S1"), Lexemes.singleChar(';'));
        g.addRule(Statement, Lexemes.literal("S2"), Lexemes.singleChar(';'));
        g.addRule(Statement, Lexemes.literal("S3"), Lexemes.singleChar(';'));

        g.addRule(SelectionStatement, If, LeftBrace, Expression, RightBrace, Statement);
        g.addRule(SelectionStatement, If, LeftBrace, Expression, RightBrace, Statement, Else, Statement);

        Parser parser  = g.createParser(All);
        parser.getDefaultLexer().filterOut(Lexemes.whitespace());

        parser.parse("if(E) S1;");
        parser.parse("if(E) S1; else S2;");
        parser.parse("if(E) S1; else if (E) S2; else S3;");
        parser.parse("if(E) S1; if (E) S2; else S3;");



    }
}
