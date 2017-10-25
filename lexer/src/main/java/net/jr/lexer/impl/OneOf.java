package net.jr.lexer.impl;

public class OneOf extends LexemeImpl {

    public OneOf(final String chars) {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
        initialState.when(c -> chars.contains("" + c)).goTo(builder.newFinalState());
        setAutomaton(builder.build());

    }


}
