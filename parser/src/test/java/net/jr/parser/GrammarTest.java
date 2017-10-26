package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.impl.Literal;
import net.jr.lexer.impl.SingleChar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.StringReader;

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
    SingleChar star = new SingleChar('*');
    SingleChar plus = new SingleChar('+');

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
        grammar.addRule(V, star, E);
    }

    @Test
    public void testParse() {
        Parser parser = grammar.createParser(grammar.getTargetSymbol());
        Lexer lexer = new Lexer(grammar.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader("x = *x")));

    }

    @Test
    public void testParse2() {

        Grammar g = new Grammar();

        // (0)
        g.addRule(S, E);

        // (1) E → E * B
        g.addRule(E, E, star, B);

        // (2) E → E + B
        g.addRule(E, E, plus, B);

        // (3) E → B
        g.addRule(E, B);

        // (4) B → 0
        g.addRule(B, zero);

        // (5) B → 1
        g.addRule(B, one);

        Parser parser = g.createParser(S);
        Lexer lexer = new Lexer(g.getTerminals());

        parser.parse(lexer.iterator(new StringReader("1+1")));
    }

    @Test
    public void testParseSimpleChoice() {
        Symbol V = new Forward("V");
        Grammar g = new Grammar();
        g.addRule(V, new SingleChar('0'));
        g.addRule(V, new SingleChar('1'));

        Parser parser = g.createParser(V);
        Lexer lexer = new Lexer(g.getTerminals());

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
        Lexer lexer = new Lexer(g.getTerminals());
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
        Lexer lexer = new Lexer(g.getTerminals());
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
        Lexer lexer = new Lexer(g.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader(">>")));

        //list of identifiers
        parser.parse(lexer.iterator(new StringReader(">>oh! eh! yeah!")));

    }

}
