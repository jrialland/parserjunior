package net.jr.lexer.automaton;

public interface Transition {

    boolean isValid(char c);

    State getNextState();
}
