package net.jr.codegen.lexer;

import net.jr.lexer.Lexemes;
import net.jr.lexer.Lexer;
import org.junit.Test;

import java.io.PrintWriter;

public class LexerGeneratorTest {

    @Test
    public void doTest() {

        Lexer lexer = Lexer.forLexemes(
                Lexemes.cInteger(),
                Lexemes.singleChar('+'),
                Lexemes.singleChar('-'),
                Lexemes.singleChar('*'),
                Lexemes.singleChar('/'),
                Lexemes.singleChar('('),
                Lexemes.singleChar(')')
        );

        new LexerGenerator().generate(lexer, new PrintWriter(System.out));

    }
}
