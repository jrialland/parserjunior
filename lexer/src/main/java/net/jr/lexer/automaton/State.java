package net.jr.lexer.automaton;

import java.util.List;

public interface State {

    List<Transition> getOutgoingTransitions();

    boolean isFinalState();
}
