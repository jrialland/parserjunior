package net.jr.lexer.expr.impl;

import net.jr.lexer.Terminal;
import net.jr.lexer.automaton.State;
import net.jr.lexer.impl.CharConstraint;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

public class Node implements State {

    private Set<net.jr.lexer.automaton.Transition> outgoingTransitions = new HashSet<>();

    private Set<net.jr.lexer.automaton.Transition> incomingTransitions = new HashSet<>();

    private net.jr.lexer.automaton.Transition fallbackTransition = null;

    private Terminal terminal = null;

    private Integer id = null;

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        if (id == null) {
            throw new IllegalStateException("Id has not been assigned. setId(int) must be called first");
        } else {
            return id;
        }

    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Transition addTransition(CharConstraint charConstraint) {
        Transition t = new Transition(this);
        t.setCharConstraint(charConstraint);
        outgoingTransitions.add(t);
        return t;
    }

    public net.jr.lexer.automaton.Transition addFallback() {
        this.fallbackTransition = new Transition(this);
        ((Transition) this.fallbackTransition).setCharConstraint(CharConstraint.Builder.any().build());
        return this.fallbackTransition;
    }

    public void setFallbackTransition(net.jr.lexer.automaton.Transition fallbackTransition) {
        this.fallbackTransition = fallbackTransition;
    }

    @Override
    public net.jr.lexer.automaton.Transition getFallbackTransition() {
        return fallbackTransition;
    }

    public void disconnect() {
        for (net.jr.lexer.automaton.Transition outTransition : outgoingTransitions) {
            ((Transition) outTransition).getTarget().getIncomingTransitions().remove(outTransition);
        }
        for (net.jr.lexer.automaton.Transition inTransition : incomingTransitions) {
            ((Transition) inTransition).getSource().getOutgoingTransitions().remove(inTransition);
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
        return terminal != null;
    }

    @Override
    public String toString() {
        if(id==null){
            StringWriter sw = new StringWriter();
            if(isFinalState()) {
                sw.append("{final}\n");
            }
            for(net.jr.lexer.automaton.Transition incoming : incomingTransitions) {
                sw.append("    incoming : " + incoming + "\n");
            }
            for(net.jr.lexer.automaton.Transition outgoing : outgoingTransitions) {
                sw.append("    outgoing : " + outgoing + "\n");
            }
            return sw.toString();
        } else {
            return Integer.toString(getId());
        }
    }
}
