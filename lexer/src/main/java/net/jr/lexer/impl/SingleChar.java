package net.jr.lexer.impl;


public class SingleChar extends LexemeImpl {

    private char character;

    public SingleChar(char character) {
        this.character = character;
    }

    @Override
    public Automaton getAutomaton() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        builder.initialState().when(c -> c == character).goTo(builder.newFinalState());
        return builder.build();
    }

    @Override
    public String toString() {
        return Character.toString(character);
    }
}
