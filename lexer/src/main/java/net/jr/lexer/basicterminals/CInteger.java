package net.jr.lexer.basicterminals;

import net.jr.lexer.Lexemes;
import net.jr.lexer.automaton.DefaultAutomaton;
import net.jr.lexer.impl.TerminalImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static net.jr.lexer.impl.CharConstraint.Builder.*;

public class CInteger extends TerminalImpl {

    public CInteger() {
        DefaultAutomaton.Builder builder = DefaultAutomaton.Builder.forTokenType(this);
        DefaultAutomaton.Builder.BuilderState init = builder.initialState();
        DefaultAutomaton.Builder.BuilderState got0 = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();

        init.when(eq('0')).goTo(got0);

        init.when(inList(Lexemes.NumbersExceptZero)).goTo(finalState);
        finalState.when(inList(Lexemes.Numbers)).goTo(finalState);

        addIntegerSuffix(builder, finalState);
        setAutomaton(builder.build());
    }

    static void addIntegerSuffix(DefaultAutomaton.Builder builder, DefaultAutomaton.Builder.BuilderState state) {
        DefaultAutomaton.Builder.BuilderState suffixU = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState suffixL = builder.newFinalState();
        DefaultAutomaton.Builder.BuilderState finalState = builder.newFinalState();
        state.when(or(eq('U'), eq('u'))).goTo(suffixU);
        suffixU.when(or(eq('L'), eq('l'))).goTo(finalState);
        state.when(or(eq('L'), eq('l'))).goTo(suffixL);
        suffixL.when(or(eq('U'), eq('u'))).goTo(finalState);
    }

    @Override
    public String toString() {
        return "CInteger";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(CInteger.class)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 8515468;
    }

    @Override
    public void marshall(DataOutputStream dataOutputStream) throws IOException {

    }

    public static CInteger unMarshall(DataInputStream in) throws IOException {
        return new CInteger();
    }
}
