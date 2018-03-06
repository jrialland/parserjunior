package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.basicterminals.Literal;
import net.jr.lexer.basicterminals.SingleChar;
import net.jr.lexer.basicterminals.Word;
import net.jr.parser.ast.AstNode;
import net.jr.parser.impl.ActionTableCaching;
import net.jr.parser.impl.LRParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Stack;

public class GrammarTest {

    @BeforeClass
    public static void setupClass() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    Symbol S = new NonTerminal("S");
    Symbol N = new NonTerminal("N");
    Symbol E = new NonTerminal("E");
    Symbol V = new NonTerminal("V");

    Symbol B = new NonTerminal("B");

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
        Parser parser = grammar.createParser(grammar.getTargetSymbol(), false);
        parser.getLexer().setFilteredOut(Lexemes.whitespace());
        parser.parse(new StringReader("x = *x"));
    }

    private Grammar makeGrammar2() {
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

        return g;
    }

    @Test
    public void testParse2() {
        Grammar g = makeGrammar2();
        Parser parser = g.createParser(S, false);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        ((LRParser) parser).parse("1+1");
    }

    @Test
    public void testHashCodeStability() {

        int h = makeGrammar2().hashCode();
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(h, makeGrammar2().hashCode());
        }

    }

    @Test
    public void testParseSimpleChoice() {
        Symbol V = new NonTerminal("V");
        Grammar g = new Grammar();
        g.addRule(V, new SingleChar('0'));
        g.addRule(V, new SingleChar('1'));

        Parser parser = g.createParser(V, false);
        parser.parse(new StringReader("0"));
        parser.parse(new StringReader("1"));
    }

    @Test
    public void testParseSimpleList() {
        Grammar g = new Grammar();
        Symbol L = new NonTerminal("list");
        Symbol I = new NonTerminal("inList");
        Symbol ident = Lexemes.cIdentifier();

        g.addRule(L, new SingleChar('('), I, new SingleChar(')'));
        g.addRule(I, ident);
        g.addRule(I, I, new SingleChar(','), ident);
        g.addEmptyRule(I);

        Parser parser = g.createParser(false);
        Lexer lexer = Lexer.forLexemes(g.getTerminals());
        lexer.setFilteredOut(Lexemes.whitespace());
        parser.setLexer(lexer);

        //empty list is ok
        parser.parse(new StringReader("()"));

        //list of identifiers
        parser.parse(new StringReader("(list, of, identifiers)"));


    }

    @Test
    public void testList() {
        Grammar g = new Grammar();
        Symbol L = new NonTerminal("list");
        g.addRule(L, new SingleChar('('), g.list(true, new SingleChar(','), Lexemes.cIdentifier()), new SingleChar(')'));

        Parser parser = g.createParser(L, false);

        parser.getLexer().setFilteredOut(Lexemes.whitespace());

        //empty list are ok
        parser.parse(new StringReader("()"));

        //list of identifiers
        parser.parse(new StringReader("(list, of, identifiers)"));

        try {
            parser.parse(new StringReader("(list"));
            Assert.fail();
        } catch (ParseError e) {
            //ok!
        }
    }

    @Test
    public void testListIsFlat() {

        Grammar g = new Grammar();

        NonTerminal listOfInts = new NonTerminal("listOfInts");
        g.addRule(listOfInts, g.list(true, Lexemes.singleChar(','), Lexemes.cInteger()));

        AstNode list = g.createParser(false).parse("51,178,1158,155").getFirstChild();
        Assert.assertEquals(4, list.getChildren().size());
        Assert.assertEquals("51", list.getChildren().get(0).asToken().getText());
        Assert.assertEquals("178", list.getChildren().get(1).asToken().getText());
        Assert.assertEquals("1158", list.getChildren().get(2).asToken().getText());
        Assert.assertEquals("155", list.getChildren().get(3).asToken().getText());
    }

    @Test
    public void testEmptyList() {
        Grammar g = new Grammar();
        NonTerminal listOfInts = new NonTerminal("listOfInts");
        g.addRule(listOfInts, g.list(true, Lexemes.singleChar(','), Lexemes.cInteger()));
        AstNode emptyList = g.createParser(false).parse("").getFirstChild();
        Assert.assertTrue(emptyList.getChildren().isEmpty());
    }

    @Test
    public void testZeroOrMore() {

        Grammar g = new Grammar();
        Symbol L = new NonTerminal("onomatopoeias");
        g.addRule(L, new Literal(">>"), g.zeroOrMore(Lexemes.cIdentifier(), new SingleChar('!')));

        Parser parser = g.createParser(L, false);
        parser.getLexer().setFilteredOut(Lexemes.whitespace());

        parser.parse(new StringReader(">>"));

        //list of identifiers
        parser.parse(new StringReader(">>oh! eh! yeah!"));

    }

    @Test
    public void testOneOrMore() {
        Grammar g = new Grammar();
        Symbol S = new NonTerminal("S");
        g.addRule(S, g.oneOrMore(new Word("nahnah")), new Word("batman"));

        Parser parser = g.createParser(S, false);
        parser.getLexer().setFilteredOut(Lexemes.whitespace());

        parser.parse(new StringReader("nahnah nahnah nahnah nahnah nahnah batman"));
        parser.parse(new StringReader("nahnah batman"));

        try {
            parser.parse(new StringReader("batman"));
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
        Symbol E = new NonTerminal("E");

        g.setPrecedenceLevel(20, mult, div);
        g.setPrecedenceLevel(10, plus, minus);

        g.addRule(E, Lexemes.cInteger()).withAction(ctx -> {
            int value = Integer.parseInt(ctx.getAstNode().asToken().getText());
            calculatorStack.push(value);
        });

        g.addRule(E, E, g.oneOf(plus, minus), E)
                .withAssociativity(Associativity.Left)
                .withAction(ctx -> {
                    int topOfStack = calculatorStack.pop();
                    int nextInStack = calculatorStack.pop();
                    String operation = ctx.getAstNode().getChildren().get(1).asToken().getText();
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
                    String operation = ctx.getAstNode().getChildren().get(1).asToken().getText();
                    switch (operation) {
                        case "*":
                            calculatorStack.push(nextInStack * topOfStack);
                            break;
                        case "/":
                            calculatorStack.push(nextInStack / topOfStack);
                            break;
                    }
                });

        Parser parser = g.createParser(E, false);
        parser.getLexer().setFilteredOut(Lexemes.whitespace());

        parser.parse(new StringReader(expression));

        return calculatorStack.pop();
    }

    @Test
    public void testStabilityNoCache() {
        ActionTableCaching.setEnabled(false);
        for (int i = 0; i < 100; i++) {
            new IfElseTest().testParseNestedElse();
        }
        ActionTableCaching.setEnabled(true);
    }

    @Test
    public void testStabilityWithCache() {
        ActionTableCaching.setEnabled(true);
        for (int i = 0; i < 100; i++) {
            LoggerFactory.getLogger(GrammarTest.class).trace("--------------------------" + i);
            new IfElseTest().testParseNestedElse();
        }
    }

    @Test
    public void testCreateParser() {
        grammar.createParser();
        grammar.createParser(N);
    }

}
