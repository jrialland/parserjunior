package net.jr.lexer.automaton;

import java.util.function.Function;

public interface Transition<T> {

    boolean isValid(char c);

    State<T> getNextState();

    Function<T, Boolean> getCondition();
}
