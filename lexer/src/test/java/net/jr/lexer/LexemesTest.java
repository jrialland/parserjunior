package net.jr.lexer;

import net.jr.lexer.basicterminals.Literal;
import org.junit.Test;

public class LexemesTest {

    @Test
    public void testToString() {
        Lexemes.eof().toString();
        Lexemes.cIdentifier().toString();
        Lexemes.whitespace().toString();
        new Literal("else").toString();
    }

}
