package net.jr.lexer.automaton;

import net.jr.lexer.Lexeme;

public interface Automaton extends Cloneable {

    Lexeme getTokenType();

    Object clone() throws CloneNotSupportedException;

    State getInitialState();
}
