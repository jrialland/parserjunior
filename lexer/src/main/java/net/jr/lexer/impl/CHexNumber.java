package net.jr.lexer.impl;

import net.jr.lexer.Lexemes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.eq;
import static net.jr.lexer.impl.CharConstraint.Builder.inList;

public class CHexNumber extends LexemeImpl {

    public CHexNumber() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        DefaultAutomaton.Builder.BuilderState nextState;

        nextState = builder.newNonFinalState();
        currentState.when(eq('0')).goTo(nextState);
        currentState = nextState;

        nextState = builder.newNonFinalState();
        currentState.when(eq('x')).goTo(nextState);
        currentState = nextState;

        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        currentState.when(inList(Lexemes.HexDigit)).goTo(finalState);
        finalState.when(inList(Lexemes.HexDigit)).goTo(finalState);
        CInteger.addIntegerSuffix(builder, finalState);
        setAutomaton(builder.build());
    }

    @Override
    public String toString() {
        return "CHexNumber";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(CHexNumber.class)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return -1785154;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static CHexNumber unMarshall(DataInputStream in) throws IOException {
        return new CHexNumber();
    }
}
