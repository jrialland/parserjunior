package net.jr.parser;

import net.jr.common.Symbol;
import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import net.jr.lexer.impl.SingleChar;
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

    SingleChar x = new SingleChar('x');
    SingleChar eq = new SingleChar('=');
    SingleChar star = new SingleChar('*');

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
        Parser parser = grammar.createParser();
        Lexer lexer = new Lexer(grammar.getTerminals());
        lexer.filterOut(Lexemes.whitespace());

        parser.parse(lexer.iterator(new StringReader("x = *x")));

    }
}
