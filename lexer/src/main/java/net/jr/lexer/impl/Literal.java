package net.jr.lexer.impl;

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
            currentState.when(x -> x == c).goTo(targetState);
            currentState = targetState;
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "t('" + value + "')";
    }
}
