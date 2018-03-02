package net.jr.codegen.js;

import net.jr.grammar.c.CGrammar;
import org.junit.Test;

import java.io.PrintWriter;

public class JsParserGeneratorTest {

    @Test
    public void testForCGrammar() {
        JsParserGenerator jsParserGenerator = new JsParserGenerator();
        jsParserGenerator.generate(new CGrammar(), new PrintWriter(System.out));
    }

}
