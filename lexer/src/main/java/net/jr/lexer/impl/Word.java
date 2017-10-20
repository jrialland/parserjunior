package net.jr.lexer.impl;

public class Word extends LexemeImpl {

    private Automaton automaton;

    public Word(String possibleChars) {
        this(possibleChars, possibleChars);
    }

    public Word(String possibleFirstChar, String possibleNextChars) {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState ok = builder.newFinalState();
        builder.initialState().when(c -> possibleFirstChar.indexOf(c) > -1).goTo(ok);
        ok.when(c -> possibleNextChars.indexOf(c) > -1).goTo(ok);
        this.automaton = builder.build();
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }
}
