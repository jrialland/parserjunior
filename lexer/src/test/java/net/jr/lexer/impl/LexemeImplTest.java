package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;
import org.junit.Test;

public class LexemeImplTest {

    @Test
    public void test() {
        Lexemes.artificial("a").getPriority();
        Lexemes.artificial("a").toString();
        ((LexemeImpl) Lexemes.artificial("a")).setPriority(0);
    }

}
