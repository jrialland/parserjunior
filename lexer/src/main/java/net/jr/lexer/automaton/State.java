package net.jr.lexer.automaton;

import net.jr.lexer.Lexeme;

import java.util.Set;

public interface State<T> {

    Set<Transition<T>> getOutgoingTransitions();

    boolean isFinalState();

    Lexeme getLexeme();
}
