package net.jr.lexer.automaton;

import net.jr.lexer.Terminal;

public interface Automaton extends Cloneable {

    Terminal getTokenType();

    Object clone() throws CloneNotSupportedException;

    State getInitialState();
}
