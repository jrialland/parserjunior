package net.jr.lexer.impl;

/**
 * Define a lexeme that can be any of the characters passed to its constructor.
 */
public class OneOf extends LexemeImpl {

    public OneOf(final String chars) {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
        initialState.when(c -> chars.contains("" + c)).goTo(builder.newFinalState());
        setAutomaton(builder.build());
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
