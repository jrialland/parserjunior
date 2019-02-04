package net.jr.lexer.automaton;

public interface Transition<T> {

    boolean isValid(T c);

    State<T> getNextState();

    Object getConstraint();
}
