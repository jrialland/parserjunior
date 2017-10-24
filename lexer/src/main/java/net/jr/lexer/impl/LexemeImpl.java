package net.jr.lexer.impl;

import net.jr.lexer.Lexeme;

public class LexemeImpl implements Lexeme {

    private Automaton automaton;

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public Automaton getAutomaton() {
        return automaton;
    }

}
