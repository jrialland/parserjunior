package net.jr.lexer.impl;

import static net.jr.lexer.impl.CharConstraint.Builder.*;
/**
 * Define a lexeme that can be any of the characters passed to its constructor.
 */
public class OneOf extends LexemeImpl {

    public OneOf(final String chars) {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState initialState = builder.initialState();
        initialState.when(inList(chars)).goTo(builder.newFinalState());
        setAutomaton(builder.build());
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
