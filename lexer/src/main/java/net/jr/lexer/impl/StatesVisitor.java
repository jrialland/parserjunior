package net.jr.lexer.impl;

import net.jr.lexer.automaton.State;
import net.jr.lexer.automaton.Transition;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

public class StatesVisitor {

    public static <T> void visit(State<T> initialState, Consumer<State<T>> consumer) {
        Stack<State<T>> toVisit = new Stack<>();
        Set<State<T>> viewed = new HashSet<>();
        toVisit.push(initialState);
        viewed.add(initialState);
        while (!toVisit.isEmpty()) {
            State<T> current = toVisit.pop();
            for (Transition<T> transition : current.getOutgoingTransitions()) {
                State<T> nextState = transition.getNextState();
                if (!viewed.contains(nextState)) {
                    toVisit.push(nextState);
                    viewed.add(nextState);
                }
            }
            consumer.accept(current);
        }
    }

}
