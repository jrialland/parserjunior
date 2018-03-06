package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;
import org.junit.Test;

public class TerminalImplTest {

    @Test
    public void test() {
        Lexemes.artificial("a").getPriority();
        Lexemes.artificial("a").toString();
        ((TerminalImpl) Lexemes.artificial("a")).setPriority(0);
    }

}
