package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;
import net.jr.lexer.automaton.Automaton;
import net.jr.marshalling.MarshallingUtil;

public abstract class LexemeImpl implements Lexeme {

    private Automaton automaton;

    private int priority;

    private String name;

    public LexemeImpl() {
        this(null);
    }

    public LexemeImpl(String name) {
        this.name = name;
        this.priority = 1;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return super.toString();
        }
    }

    @Override
    public Lexeme withPriority(int priority) {
        try {
            LexemeImpl clone = MarshallingUtil.copyOf(this);
            return clone;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
