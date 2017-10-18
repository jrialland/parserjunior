package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;

public interface Automaton {

    boolean step(char c);

    void reset();

    int getMatchedLength();

    boolean isInFinalState();

    Lexeme getTokenType();
}
