package net.jr.lexer.basicterminals;

import net.jr.lexer.Lexemes;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.CharConstraint;
import net.jr.lexer.impl.TerminalImpl;

import java.io.IOException;

public class CHexNumber extends TerminalImpl {

    public CHexNumber() {
        setName("cHexNumber");
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

    public static CHexNumber unMarshall(java.io.DataInput in) throws IOException {
        return TerminalImpl.unMarshall(new CHexNumber(), in);
    }

    @Override
    public String toString() {
        return "cHexNumber";
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

}
