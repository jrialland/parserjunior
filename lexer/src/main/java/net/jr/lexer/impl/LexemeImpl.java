package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;

public class LexemeImpl implements Lexeme {

    private Automaton automaton;

    private int priority;

    public LexemeImpl() {
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

}
