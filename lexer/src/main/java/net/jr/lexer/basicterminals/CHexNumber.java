package net.jr.lexer.basicterminals;

import net.jr.lexer.Lexemes;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CHexNumber extends TerminalImpl {

    public CHexNumber() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState currentState = builder.initialState();
        DefaultAutomaton.Builder.BuilderState nextState;

        nextState = builder.newNonFinalState();
        currentState.when(CharConstraint.Builder.eq('0')).goTo(nextState);
        currentState = nextState;

        nextState = builder.newNonFinalState();
        currentState.when(CharConstraint.Builder.eq('x')).goTo(nextState);
        currentState = nextState;

        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        currentState.when(CharConstraint.Builder.inList(Lexemes.HexDigit)).goTo(finalState);
        finalState.when(CharConstraint.Builder.inList(Lexemes.HexDigit)).goTo(finalState);
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
