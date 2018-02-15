package net.jr.lexer.automaton;

import java.util.Set;

public interface State {

    Set<Transition> getOutgoingTransitions();

    boolean isFinalState();
}
