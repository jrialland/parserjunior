package net.jr.lexer;

import net.jr.lexer.basicterminals.Literal;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class LexemesTest {

    @Test
    public void testToString() {
        Lexemes.eof().toString();
        Lexemes.cIdentifier().toString();
        Lexemes.whitespace().toString();
        new Literal("else").toString();
    }

    @Test
    public void testCInteger() {
        List<Token> tokens = Lexer.forLexemes(Lexemes.cInteger()).tokenize("0ul");
        System.out.println(tokens);
    }

}
