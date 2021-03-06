package net.jr.lexer.automaton;

import net.jr.lexer.Terminal;

import java.util.Set;

public interface State<T> {

    Set<Transition<T>> getOutgoingTransitions();

    Transition<T> getFallbackTransition();

    boolean isFinalState();

    Terminal getTerminal();

    int getId();

    void setId(int id);
}
