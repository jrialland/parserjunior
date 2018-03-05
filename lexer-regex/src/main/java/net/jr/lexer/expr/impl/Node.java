package net.jr.lexer.expr.impl;

import net.jr.lexer.Lexeme;
import net.jr.lexer.automaton.State;
import net.jr.lexer.impl.CharConstraint;

import java.util.HashSet;
import java.util.Set;

public class Node implements State {

    private Set<net.jr.lexer.automaton.Transition> outgoingTransitions = new HashSet<>();

    private Set<net.jr.lexer.automaton.Transition> incomingTransitions = new HashSet<>();

    private boolean finalState = false;

    @Override
    public Lexeme getLexeme() {
        throw new IllegalStateException();
    }

    public Transition addTransition(CharConstraint charConstraint) {
        Transition t = new Transition(this);
        t.setCharConstraint(charConstraint);
        outgoingTransitions.add(t);
        return t;
    }

    public void disconnect() {
        for (net.jr.lexer.automaton.Transition outTransition : outgoingTransitions) {
            ((Transition)outTransition).getTarget().getIncomingTransitions().remove(outTransition);
        }
        for (net.jr.lexer.automaton.Transition inTransition : incomingTransitions) {
            ((Transition)inTransition).getSource().getOutgoingTransitions().remove(inTransition);
        }
    }

    public Transition addTransition(CharConstraint.Builder builder) {
        return addTransition(builder.build());
    }

    public Set<net.jr.lexer.automaton.Transition> getOutgoingTransitions() {
        return outgoingTransitions;
    }

    public Set<net.jr.lexer.automaton.Transition> getIncomingTransitions() {
        return incomingTransitions;
    }

    @Override
    public boolean isFinalState() {
        return finalState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }
}
