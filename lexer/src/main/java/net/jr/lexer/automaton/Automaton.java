package net.jr.lexer.automaton;

import net.jr.lexer.Lexeme;

public interface Automaton extends Cloneable {

    boolean step(char c);

    void reset();

    int getMatchedLength();

    boolean isInFinalState();

    Lexeme getTokenType();

    Object clone() throws CloneNotSupportedException;

    State getInitialState();
}
