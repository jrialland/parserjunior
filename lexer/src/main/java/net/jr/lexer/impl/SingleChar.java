package net.jr.lexer.impl;


import net.jr.lexer.CharConstraint;

public class SingleChar extends LexemeImpl {

    private char character;

    public SingleChar(char character) {
        this.character = character;
    }

    @Override
    public Automaton getAutomaton() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        builder.initialState().when(CharConstraint.Builder.eq(character)).goTo(builder.newFinalState());
        return builder.build();
    }

    @Override
    public String toString() {
        return "'" + Character.toString(character) + "'";
    }

    @Override
    public int hashCode() {
        return character + 1844;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(SingleChar.class)) {
            return false;
        }
        return ((SingleChar) obj).character == character;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
