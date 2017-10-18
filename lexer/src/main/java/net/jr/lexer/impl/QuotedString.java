package net.jr.lexer.impl;

import java.util.Arrays;

public class QuotedString extends LexemeImpl {

    private Automaton automaton;

    public QuotedString(char startChar, char endChar, char escapeChar, char[] forbiddenChars) {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState inString = builder.newNonFinalState();
        DefaultAutomaton.Builder.BuilderState escaping = builder.newNonFinalState();
        builder.initialState().when(c -> c == startChar).goTo(inString);
        inString.when(c -> c == escapeChar).goTo(escaping);
        inString.when(c -> Arrays.binarySearch(forbiddenChars, c) > -1).goTo(builder.failedState());
        inString.when(c -> c != endChar).goTo(inString);
        escaping.when(c -> true).goTo(inString);
        inString.when(c -> c == endChar).goTo(builder.newFinalState());
        this.automaton = builder.build();
    }


    @Override
    public Automaton getAutomaton() {
        return automaton;
    }
}
