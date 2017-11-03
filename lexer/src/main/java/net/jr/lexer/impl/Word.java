package net.jr.lexer.impl;

import net.jr.lexer.CharConstraint;

public class Word extends LexemeImpl {

    private Automaton automaton;

    private String name;

    @Override
    public String toString() {
        return name;
    }

    public Word(String possibleChars) {
        this(possibleChars, possibleChars);
    }

    public Word(String possibleFirstChar, String possibleNextChars) {
        this(possibleFirstChar, possibleNextChars, null);
    }

    public Word(String possibleFirstChar, String possibleNextChars, String name) {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState ok = builder.newFinalState();
        builder.initialState().when(CharConstraint.Builder.inList(possibleFirstChar)).goTo(ok);
        ok.when(CharConstraint.Builder.inList(possibleNextChars)).goTo(ok);
        this.automaton = builder.build();
        if (name == null) {
            if (possibleFirstChar.equals(possibleNextChars)) {
                this.name = String.format("Word('%s')", possibleFirstChar);
            } else {
                this.name = String.format("Word('%s', '%s')", possibleFirstChar, possibleNextChars);
            }
        } else {
            this.name = name;
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }
}
