package net.jr.lexer.impl;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;

/**
 * A literal is a 'keyword' i.e a fixed string.
 */
public class Literal extends LexemeImpl {

    private String value;

    public Literal(String value) {
        this.value = value;
    }

    @Override
    public Automaton getAutomaton() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        char[] chars = value.toCharArray();
        for (int i = 0, max = chars.length; i < max; i++) {
            char c = chars[i];
            final DefaultAutomaton.Builder.BuilderState targetState;
            if (i == max - 1) {
                targetState = builder.newFinalState();
            } else {
                targetState = builder.newNonFinalState();
            }
            currentState.when(eq(c)).goTo(targetState);
            currentState = targetState;
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
