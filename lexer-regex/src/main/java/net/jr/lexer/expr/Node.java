package net.jr.lexer.expr;

import net.jr.lexer.impl.CharConstraint;

import java.util.HashSet;
import java.util.Set;

public class Node {

    private Set<Transition> outgoingTransitions = new HashSet<>();

    private Set<Transition> incomingTransitions = new HashSet<>();

    private boolean finalState = false;

    public Transition addTransition(CharConstraint charConstraint) {
        Transition t = new Transition(this);
        t.setCharConstraint(charConstraint);
        outgoingTransitions.add(t);
        return t;
    }

    public Transition addTransition(CharConstraint.Builder builder) {
        return addTransition(builder.build());
    }

    public Set<Transition> getOutgoingTransitions() {
        return outgoingTransitions;
    }

    public Set<Transition> getIncomingTransitions() {
        return incomingTransitions;
    }

    public boolean isFinalState() {
        return finalState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }
}
