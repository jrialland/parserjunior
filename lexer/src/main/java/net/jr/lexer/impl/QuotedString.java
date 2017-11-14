package net.jr.lexer.impl;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class QuotedString extends LexemeImpl {

    private Automaton automaton;

    private char starChar;

    private char endChar;

    private char escapeChar;

    private char[] forbiddenChars;

    public QuotedString(char startChar, char endChar, char escapeChar, char[] forbiddenChars) {
        this.starChar = startChar;
        this.endChar = endChar;
        this.escapeChar = escapeChar;
        this.forbiddenChars = forbiddenChars;
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState inString = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState escaping = builder.newNonFinalState();
        builder.initialState().when(eq(startChar)).goTo(inString);
        inString.when(eq(escapeChar)).goTo(escaping);
        inString.when(inList(forbiddenChars)).goTo(builder.failedState());
        inString.when(not(eq(endChar))).goTo(inString);
        escaping.when(any()).goTo(inString);
        inString.when(eq(endChar)).goTo(builder.newFinalState());
        this.automaton = builder.build();
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
